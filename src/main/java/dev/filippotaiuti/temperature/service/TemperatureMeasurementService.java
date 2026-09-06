package dev.filippotaiuti.temperature.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;

@Service
public class TemperatureMeasurementService
{
    private final TemperatureMeasurementRepository repository;

    public TemperatureMeasurementService(TemperatureMeasurementRepository repository)
    {
        this.repository = repository;
    }

    public TemperatureMeasurement save(TemperatureMeasurementRequest request)
    {
        TemperatureMeasurement measurement = new TemperatureMeasurement(
                request.getTemperature(),
                request.getUnit(),
                request.getMeasuredAt(),
                request.getSensorId());
        return repository.save(measurement);
    }

    public List<TemperatureMeasurement> findAll()
    {
        return repository.findAll();
    }
}