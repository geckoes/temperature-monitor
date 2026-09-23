package dev.filippotaiuti.temperature.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String sort)
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
        if (page < 0)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page must be greater than or equal to 0");
        if (size <= 0)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size must be greater than 0");

        if (size > MAX_PAGE_SIZE)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size must be lower than " + MAX_PAGE_SIZE);

        Sort.Order firstSort = Sort.Order.desc("measuredAt");
        Sort.Order secondSort = Sort.Order.asc("sensorId");
        if (sort != null) {
            String[] sortParts = sort.split(",");
            switch (sort) {
                case "measuredAt,asc":
                    firstSort = Sort.Order.asc(sortParts[0]);
                    secondSort = Sort.Order.asc("sensorId");
                    break;
                case "measuredAt,desc":
                    break;
                case "sensorId,asc":
                    firstSort = Sort.Order.asc(sortParts[0]);
                    secondSort = Sort.Order.desc("measuredAt");
                    break;
                case "sensorId,desc":
                    firstSort = Sort.Order.desc(sortParts[0]);
                    secondSort = Sort.Order.desc("measuredAt");
                    break;
                default:
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "sort order is not written correctly");
            }
        }

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                firstSort,
                secondSort
            )
        );

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