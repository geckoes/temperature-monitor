package dev.filippotaiuti.temperature.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;


@Service
public class TemperatureMeasurementService {
    private final TemperatureMeasurementRepository repository;
    
    public TemperatureMeasurementService(TemperatureMeasurementRepository repository) {
        this.repository = repository;
    }

    public TemperatureMeasurement save(TemperatureMeasurementRequest request) {
        TemperatureMeasurement measurement = new TemperatureMeasurement(
            request.getTemperature(),
            request.getUnit(),
            OffsetDateTime.now(ZoneOffset.UTC),
            request.getSensorId()
        );
        return repository.save(measurement);
    }
}