package dev.filippotaiuti.temperature.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public List<TemperatureMeasurementResponse> getMeasurements(@RequestParam(required = false) String sensorId)
    {
        List<TemperatureMeasurement> measurements = sensorId == null
                ? service.findAll()
                : service.findBySensorId(sensorId);

        return measurements.stream()
                .map(TemperatureMeasurementResponse::from)
                .toList();
    }
}