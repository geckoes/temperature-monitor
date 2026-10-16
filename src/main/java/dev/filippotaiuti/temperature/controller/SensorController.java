package dev.filippotaiuti.temperature.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.filippotaiuti.temperature.dto.SensorRegistrationRequest;
import dev.filippotaiuti.temperature.dto.SensorResponse;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.mapper.SensorResponseMapper;
import dev.filippotaiuti.temperature.service.SensorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final SensorService sensorService;
    private final SensorResponseMapper mapper;

    public SensorController(SensorService sensorService, SensorResponseMapper mapper) {
        this.sensorService = sensorService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<SensorResponse> register(
        @Valid @RequestBody SensorRegistrationRequest request) {

        Sensor sensor = sensorService.register(request);
        SensorResponse response = mapper.toResponse(sensor);

        URI location = URI.create("/api/sensors/" + sensor.getSensorId());

        return ResponseEntity.created(location).body(response);
    }
}