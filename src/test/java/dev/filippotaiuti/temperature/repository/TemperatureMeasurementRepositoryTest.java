package dev.filippotaiuti.temperature.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class TemperatureMeasurementRepositoryTest
{
    @Autowired
    private TemperatureMeasurementRepository repository;

    @Test
    void testFindBySensorId()
    {
        // Arrange
        TemperatureMeasurement first = new TemperatureMeasurement(
                new BigDecimal("21.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.now(),
                "sensor-001");
        TemperatureMeasurement second = new TemperatureMeasurement(
                new BigDecimal("72.5"),
                TemperatureUnit.FAHRENHEIT,
                OffsetDateTime.now(),
                "sensor-001");

        TemperatureMeasurement third = new TemperatureMeasurement(
                new BigDecimal("22.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.now(),
                "sensor-002");

        repository.save(first);
        repository.save(second);
        repository.save(third);

        // Act
        List<TemperatureMeasurement> result = repository.findBySensorId("sensor-001");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(m -> "sensor-001".equals(m.getSensorId())));
    }

    @Test
    void testFindBySensorIdAndMeasuredAtBetween()
    {
        // Arrange
        TemperatureMeasurement first = new TemperatureMeasurement(
                new BigDecimal("21.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.parse("2026-09-07T10:00:00Z"),
                "sensor-001");
        TemperatureMeasurement second = new TemperatureMeasurement(
                new BigDecimal("72.5"),
                TemperatureUnit.FAHRENHEIT,
                OffsetDateTime.parse("2026-09-07T11:00:00Z"),
                "sensor-001");

        TemperatureMeasurement third = new TemperatureMeasurement(
                new BigDecimal("22.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.parse("2026-09-07T13:00:00Z"),
                "sensor-001");

        repository.save(first);
        repository.save(second);
        repository.save(third);

        // Act
        List<TemperatureMeasurement> result = repository.findBySensorIdAndMeasuredAtBetween(
                "sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"),
                OffsetDateTime.parse("2026-09-07T12:00:00Z"));

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(m -> !m.getMeasuredAt().isBefore(
                        OffsetDateTime.parse("2026-09-07T10:00:00Z"))
                        &&
                        !m.getMeasuredAt().isAfter(
                                OffsetDateTime.parse("2026-09-07T12:00:00Z"))));
        assertFalse(result.contains(third));
    }
}
