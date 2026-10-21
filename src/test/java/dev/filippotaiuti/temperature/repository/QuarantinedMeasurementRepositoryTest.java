package dev.filippotaiuti.temperature.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.filippotaiuti.temperature.entity.QuarantinedMeasurement;
import dev.filippotaiuti.temperature.entity.QuarantinedReason;
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


    }

}
