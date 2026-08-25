package dev.filippotaiuti.temperature.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.service.TemperatureMeasurementService;


@RestController
@RequestMapping("/api/measurements")
public class TemperatureMeasurementController {

    private final TemperatureMeasurementService service;

    public TemperatureMeasurementController(TemperatureMeasurementService service) {
        this.service = service;
    }

    @PostMapping
    public TemperatureMeasurement createMeasurement(@RequestBody TemperatureMeasurementRequest request) {
        return service.save(request);
    }
}