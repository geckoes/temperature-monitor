package dev.filippotaiuti.temperature.service;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;
import dev.filippotaiuti.temperature.repository.SensorRepository;

@Service
public class TemperatureMeasurementService
{
    private final TemperatureMeasurementRepository repository;
    private final SensorRepository sensorRepository;

    public TemperatureMeasurementService(TemperatureMeasurementRepository repository, SensorRepository sensorRepository)
    {
        this.repository = repository;
        this.sensorRepository = sensorRepository;
    }

    public TemperatureMeasurement save(TemperatureMeasurementRequest request)
    {
        sensorRepository.findById(request.getSensorId());

        TemperatureMeasurement measurement = new TemperatureMeasurement(
                request.getTemperature(),
                request.getUnit(),
                request.getMeasuredAt(),
                request.getSensorId(),
                request.getTerminalId()
        );
        return repository.save(measurement);
    }

    public Page<TemperatureMeasurement> findAll(Pageable pageable)
    {
        return repository.findAll(pageable);
    }

    public Page<TemperatureMeasurement> findBySensorId(String sensorId, Pageable pageable)
    {
        return repository.findBySensorId(sensorId, pageable);
    }

    /**
     * @param sensorId
     * @param offsetDateTimeFrom
     * @param offsetDateTimeTo
     * @return
     */
    public Page<TemperatureMeasurement> findBySensorIdAndMeasuredAtBetween(String sensorId,
            OffsetDateTime offsetDateTimeFrom,
            OffsetDateTime offsetDateTimeTo,
            Pageable pageable)
    {
        return repository.findBySensorIdAndMeasuredAtBetween(sensorId,
                offsetDateTimeFrom, offsetDateTimeTo, pageable);
    }

}