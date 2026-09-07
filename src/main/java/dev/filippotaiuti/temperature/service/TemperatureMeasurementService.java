package dev.filippotaiuti.temperature.service;

import java.time.OffsetDateTime;
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

    public List<TemperatureMeasurement> findBySensorId(String sensorId)
    {
        return repository.findBySensorId(sensorId);
    }

    /**
     * @param sensorId
     * @param offsetDateTimeFrom
     * @param offsetDateTimeTo
     * @return
     */
    public List<TemperatureMeasurement> findBySensorIdAndMeasuredAtBetween(String sensorId,
            OffsetDateTime offsetDateTimeFrom,
            OffsetDateTime offsetDateTimeTo)
    {
        return repository.findBySensorIdAndMeasuredAtBetween(sensorId,
                offsetDateTimeFrom, offsetDateTimeTo);
    }

}