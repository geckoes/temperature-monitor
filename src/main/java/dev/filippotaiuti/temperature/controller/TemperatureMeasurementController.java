package dev.filippotaiuti.temperature.controller;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.filippotaiuti.temperature.dto.MeasurementIngestionResult;
import dev.filippotaiuti.temperature.dto.PagedResponse;
import dev.filippotaiuti.temperature.dto.QuarantinedMeasurementResponse;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementResponse;
import dev.filippotaiuti.temperature.entity.MeasurementIngestionStatus;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.exception.IncompleteTimeRangeException;
import dev.filippotaiuti.temperature.exception.InvalidTimeRangeException;
import dev.filippotaiuti.temperature.exception.SensorIdRequiredException;
import dev.filippotaiuti.temperature.exception.UnsupportedSortPropertyException;
import dev.filippotaiuti.temperature.mapper.TemperatureMeasurementResponseMapper;
import dev.filippotaiuti.temperature.service.TemperatureMeasurementService;
import dev.filippotaiuti.temperature.validation.PaginationParameterValidator;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/measurements")
public class TemperatureMeasurementController
{
    private final TemperatureMeasurementService service;

    private final TemperatureMeasurementResponseMapper mapper;

    private final PaginationParameterValidator paginationParameterValidator;

    public TemperatureMeasurementController(TemperatureMeasurementService service,
            TemperatureMeasurementResponseMapper mapper, PaginationParameterValidator paginationParameterValidator)
    {
        this.service = service;
        this.mapper = mapper;
        this.paginationParameterValidator = paginationParameterValidator;
    }

    @PostMapping
    public ResponseEntity<?> createMeasurement(@Valid @RequestBody TemperatureMeasurementRequest request)
    {
        MeasurementIngestionResult result = service.save(request);

        if (result.status() == MeasurementIngestionStatus.QUARANTINED) {
            return ResponseEntity.ok(new QuarantinedMeasurementResponse(
                result.status(),
                result.reason()));
        }
        return ResponseEntity.ok(
            TemperatureMeasurementResponse.from(result.measurement()));
    }

    @GetMapping
    public PagedResponse<TemperatureMeasurementResponse> getMeasurements(
            @RequestParam(required = false) String sensorId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false, name = "page") String rawPage,
            @RequestParam(required = false, name = "size") String rawSize,
            @PageableDefault(page = 0, size = 50) @SortDefault.SortDefaults({
                    @SortDefault(sort = "measuredAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "sensorId", direction = Sort.Direction.ASC)
            }) Pageable pageable)
    {
        paginationParameterValidator.validate(rawPage, rawSize);

        boolean hasFrom = from != null;
        boolean hasTo = to != null;
        boolean hasTimeRange = hasFrom || hasTo;

        Sort sort = pageable.getSort();

        // 1. validate allowed properties
        if (hasTimeRange && sensorId == null)
            throw new SensorIdRequiredException();

        if (hasFrom != hasTo)
            throw new IncompleteTimeRangeException();

        if (hasFrom && hasTo && from.isAfter(to))
            throw new InvalidTimeRangeException();

        sort.forEach(order ->
        {
            if (!order.getProperty().equals("measuredAt") && !order.getProperty().equals("sensorId"))
                throw new UnsupportedSortPropertyException(order.getProperty());
        });

        // 2. determine which sort properties are already present
        boolean hasSensorId = sort.stream()
                .anyMatch(order -> order.getProperty().equals("sensorId"));

        boolean hasMeasuredAt = sort.stream()
                .anyMatch(order -> order.getProperty().equals("measuredAt"));

        // 3. append deterministic fallback sorting
        if (!hasSensorId)
        {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "sensorId"));
        }

        if (!hasMeasuredAt)
        {
            sort = sort.and(Sort.by(Sort.Direction.DESC, "measuredAt"));
        }
        // 4. rebuild pageable
        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort);

        Page<TemperatureMeasurement> measurements;

        if (sensorId != null && from != null && to != null)
            measurements = service.findBySensorIdAndMeasuredAtBetween(sensorId, from, to, pageable);
        else if (sensorId != null)
            measurements = service.findBySensorId(sensorId, pageable);
        else
            measurements = service.findAll(pageable);

        return mapper.toPagedResponse(measurements);
    }

}
