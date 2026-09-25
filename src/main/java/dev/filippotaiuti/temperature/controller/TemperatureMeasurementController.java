package dev.filippotaiuti.temperature.controller;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dev.filippotaiuti.temperature.dto.PagedResponse;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementResponse;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.mapper.TemperatureMeasurementResponseMapper;
import dev.filippotaiuti.temperature.service.TemperatureMeasurementService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/measurements")
public class TemperatureMeasurementController
{
    private static final int MAX_PAGE_SIZE = 1000;
    
    private final TemperatureMeasurementService service;

    private final TemperatureMeasurementResponseMapper mapper;

    public TemperatureMeasurementController(TemperatureMeasurementService service, TemperatureMeasurementResponseMapper mapper)
    {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public TemperatureMeasurementResponse createMeasurement(@Valid @RequestBody TemperatureMeasurementRequest request)
    {
        TemperatureMeasurement saved = service.save(request);
        return TemperatureMeasurementResponse.from(saved);
    }

    @GetMapping
    public PagedResponse<TemperatureMeasurementResponse> getMeasurements(
            @RequestParam(required = false) String sensorId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @PageableDefault(page = 0, size = 50)
            @SortDefault.SortDefaults({
                @SortDefault(sort = "measuredAt", direction = Sort.Direction.DESC),
                @SortDefault(sort = "sensorId", direction = Sort.Direction.ASC)
            }) Pageable pageable)
    {
        boolean hasFrom = from != null;
        boolean hasTo = to != null;
        boolean hasTimeRange = hasFrom || hasTo;
        if (hasTimeRange && sensorId == null)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sensorId is required when isung a time range");

        if (hasFrom != hasTo)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "from and to must be provided together");

        if (from != null && from.isAfter(to))
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "from must not be after to");
        
        Sort sort = pageable.getSort();
        boolean hasSensorId = sort.stream()
            .anyMatch(order -> order.getProperty().equals("sensorId"));

        boolean hasMeasuredAt = sort.stream()
            .anyMatch(order -> order.getProperty().equals("measuredAt"));
        
        if (!hasSensorId) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "sensorId"));
        }

        if (!hasMeasuredAt) {
            sort = sort.and(Sort.by(Sort.Direction.DESC, "measuredAt"));
        }
        pageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort
        );

        sort.stream().forEach(order -> {
            if (!order.getProperty().equals("measuredAt") && !order.getProperty().equals("sensorId"))
                throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Sorting by " + order.getProperty() + " is not allowed");
        });

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