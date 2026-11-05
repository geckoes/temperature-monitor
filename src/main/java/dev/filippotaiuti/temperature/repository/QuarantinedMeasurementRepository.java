package dev.filippotaiuti.temperature.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.filippotaiuti.temperature.entity.QuarantinedMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;

public interface QuarantinedMeasurementRepository extends JpaRepository<QuarantinedMeasurement, Long> {

    public Optional<QuarantinedMeasurement> findBySensorIdAndTerminalIdAndMeasuredAtAndTemperatureAndUnit(
        String sensorId,
        String terminalId,
        OffsetDateTime measurementAt,
        BigDecimal temperature,
        TemperatureUnit unit
    );

}
