package dev.filippotaiuti.temperature.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementResponse;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.service.TemperatureMeasurementService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/measurements")
public class TemperatureMeasurementController
{

    private final TemperatureMeasurementService service;

    public TemperatureMeasurementController(TemperatureMeasurementService service)
    {
        this.service = service;
    }

    @PostMapping
    public TemperatureMeasurementResponse createMeasurement(@Valid @RequestBody TemperatureMeasurementRequest request)
    {
        TemperatureMeasurement saved = service.save(request);
        return TemperatureMeasurementResponse.from(saved);
    }

    @GetMapping
    public List<TemperatureMeasurementResponse> getMeasurements(
            @RequestParam(required = false) String sensorId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to)
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

        List<TemperatureMeasurement> measurements;

        if (sensorId != null && from != null && to != null)
            measurements = service.findBySensorIdAndMeasuredAtBetween(sensorId, from, to);
        else if (sensorId != null)
            measurements = service.findBySensorId(sensorId);
        else
            measurements = service.findAll();

        return measurements.stream()
                .map(TemperatureMeasurementResponse::from)
                .toList();
    }

}