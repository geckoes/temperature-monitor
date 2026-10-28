package dev.filippotaiuti.temperature.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import dev.filippotaiuti.temperature.entity.QuarantinedMeasurement;
import dev.filippotaiuti.temperature.entity.QuarantinedReason;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class QuarantinedMeasurementRepositoryTest {

    @Autowired
    private QuarantinedMeasurementRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveInQuarantine() {
        // Arrange
        QuarantinedMeasurement quarantined = new QuarantinedMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            "sensor_unknown",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T09:00:00Z"),
            QuarantinedReason.UNKNOWN_SENSOR,
            OffsetDateTime.parse("2026-10-21T09:10:00Z")
        );

        // Act
        QuarantinedMeasurement saved = repository.save(quarantined);
        entityManager.flush();
        entityManager.clear();

        QuarantinedMeasurement result = repository.findById(saved.getId()).orElseThrow();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(QuarantinedReason.UNKNOWN_SENSOR, result.getReason());
        assertEquals(quarantined.getSensorId(), result.getSensorId());
        assertEquals(quarantined.getUnit(), result.getUnit());
        assertEquals(0, quarantined.getTemperature().compareTo(result.getTemperature()));
        assertEquals(quarantined.getTerminalId(), result.getTerminalId());
        assertEquals(quarantined.getMeasuredAt().toInstant(), result.getMeasuredAt().toInstant());
        assertEquals(quarantined.getReceivedAt().toInstant(), result.getReceivedAt().toInstant());
    }

    @Test
    void testFindMeasurementInQuarantine() {
        // Arrange
        QuarantinedMeasurement quarantined = new QuarantinedMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor_unknown",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T09:00:00Z"),
            QuarantinedReason.UNKNOWN_SENSOR,
            OffsetDateTime.parse("2026-10-21T09:10:00Z")
        );

        // Act
        QuarantinedMeasurement saved = repository.save(quarantined);
        entityManager.flush();
        entityManager.clear();

        QuarantinedMeasurement result = repository.findBySensorIdAndTerminalIdAndMeasuredAtAndTemperatureAndUnit(
            saved.getSensorId(),
            saved.getTerminalId(),
            saved.getMeasuredAt(),
            new BigDecimal("23.50"),
            saved.getUnit()
        ).orElseThrow();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(saved.getId(), result.getId());
        assertEquals(QuarantinedReason.UNKNOWN_SENSOR, result.getReason());
        assertEquals(quarantined.getSensorId(), result.getSensorId());
        assertEquals(quarantined.getUnit(), result.getUnit());
        assertEquals(0, quarantined.getTemperature().compareTo(result.getTemperature()));
        assertEquals(quarantined.getTerminalId(), result.getTerminalId());
        assertEquals(quarantined.getMeasuredAt().toInstant(), result.getMeasuredAt().toInstant());
        assertEquals(quarantined.getReceivedAt().toInstant(), result.getReceivedAt().toInstant());
    }

    @Test
    void testRejectDuplicateQuarantinedMeasurement() {
        // Arrange
        QuarantinedMeasurement firstQuarantined = new QuarantinedMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor_unknown",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T09:00:00Z"),
            QuarantinedReason.UNKNOWN_SENSOR,
            OffsetDateTime.parse("2026-10-21T09:10:00Z")
        );

        // Act
        repository.save(firstQuarantined);
        entityManager.flush();
        entityManager.clear();

        // Arrange
        QuarantinedMeasurement secondQuarantined = new QuarantinedMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor_unknown",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T09:00:00Z"),
            QuarantinedReason.UNKNOWN_SENSOR,
            OffsetDateTime.parse("2026-10-21T09:20:00Z")
        );

        // Act + Assert
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(secondQuarantined));
    }

    @Test
    void testSaveQuarantinedMeasurementWithDifferentTemperature() {
        // Arrange
        QuarantinedMeasurement firstQuarantined = new QuarantinedMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor_unknown",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T09:00:00Z"),
            QuarantinedReason.UNKNOWN_SENSOR,
            OffsetDateTime.parse("2026-10-21T09:10:00Z")
        );

        QuarantinedMeasurement secondQuarantined = new QuarantinedMeasurement(
            new BigDecimal("24.0"),
            TemperatureUnit.CELSIUS,
            "sensor_unknown",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T09:00:00Z"),
            QuarantinedReason.UNKNOWN_SENSOR,
            OffsetDateTime.parse("2026-10-21T09:20:00Z")
        );

        // Act
        QuarantinedMeasurement firstSaved = repository.saveAndFlush(firstQuarantined);
        QuarantinedMeasurement secondSaved = repository.saveAndFlush(secondQuarantined);

        // Assert
        assertNotEquals(firstSaved.getId(), secondSaved.getId());
    }

}
