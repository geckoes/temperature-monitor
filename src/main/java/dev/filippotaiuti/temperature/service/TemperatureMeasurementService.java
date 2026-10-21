package dev.filippotaiuti.temperature.service;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.filippotaiuti.temperature.dto.MeasurementIngestionResult;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.MeasurementIngestionStatus;
import dev.filippotaiuti.temperature.entity.QuarantinedMeasurement;
import dev.filippotaiuti.temperature.entity.QuarantinedReason;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.repository.QuarantinedMeasurementRepository;
import dev.filippotaiuti.temperature.repository.SensorRepository;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;

@Service
public class TemperatureMeasurementService
{
    private final TemperatureMeasurementRepository repository;
    private final SensorRepository sensorRepository;
    private final QuarantinedMeasurementRepository quarantinedRepository;

    public TemperatureMeasurementService(TemperatureMeasurementRepository repository, SensorRepository sensorRepository, QuarantinedMeasurementRepository quarantinedRepository)
    {
        this.repository = repository;
        this.sensorRepository = sensorRepository;
        this.quarantinedRepository = quarantinedRepository;
    }

    public MeasurementIngestionResult save(TemperatureMeasurementRequest request)
    {
        Optional<TemperatureMeasurement> existsing = repository.findBySensorIdAndMeasuredAt(
            request.getSensorId(), request.getMeasuredAt());
        if (existsing.isPresent()) {
            TemperatureMeasurement stored = existsing.get();

            boolean sameContent = stored.getTemperature().compareTo(request.getTemperature()) == 0
                && stored.getUnit() == request.getUnit()
                && Objects.equals(stored.getTerminalId(), request.getTerminalId());

            if (sameContent) {
                return new MeasurementIngestionResult(
                    MeasurementIngestionStatus.ALREADY_STORED,
                    stored,
                    null
                );
            }

        }
        Optional<Sensor> sensor = sensorRepository.findById(request.getSensorId());
        if (sensor.isEmpty()) {
            return quarantine(request, QuarantinedReason.UNKNOWN_SENSOR);
        }
        if (sensor.get().getTerminalId() == null) {
            return quarantine(request, QuarantinedReason.TERMINAL_NOT_ASSIGNED);
        }
        if (!Objects.equals(sensor.get().getTerminalId(), request.getTerminalId())) {
            return quarantine(request, QuarantinedReason.TERMINAL_MISMATCH);
        }

        TemperatureMeasurement measurement = new TemperatureMeasurement(
                request.getTemperature(),
                request.getUnit(),
                request.getMeasuredAt(),
                request.getSensorId(),
                request.getTerminalId()
        );
        TemperatureMeasurement saved = repository.save(measurement);

        return new MeasurementIngestionResult(
            MeasurementIngestionStatus.STORED,
            saved,
            null
        );
    }

    private MeasurementIngestionResult quarantine(TemperatureMeasurementRequest request, QuarantinedReason reason) {
            QuarantinedMeasurement quarantined = new QuarantinedMeasurement(
                request.getTemperature(),
                request.getUnit(),
                request.getSensorId(),
                request.getTerminalId(),
                request.getMeasuredAt(),
                reason,
                OffsetDateTime.now()
            );

            quarantinedRepository.save(quarantined);

            return new MeasurementIngestionResult(
                MeasurementIngestionStatus.QUARANTINED,
                null,
                reason
            );

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