package dev.filippotaiuti.temperature.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        Pageable pageable = PageRequest.of(
                0,
                1,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")
                )
        );
        // Act
        Page<TemperatureMeasurement> result = repository.findBySensorId("sensor-001", pageable);

        // Assert
        assertEquals(1, result.getNumberOfElements());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(1, result.getSize());
        assertEquals(2, result.getTotalPages());

        assertTrue(result.getContent().stream()
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

        Pageable pageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")
                )
        );
        // Act
        Page<TemperatureMeasurement> result = repository.findBySensorIdAndMeasuredAtBetween(
                "sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"),
                OffsetDateTime.parse("2026-09-07T12:00:00Z"),
                pageable);

        // Assert
        assertEquals(2, result.getNumberOfElements());
        assertEquals(2, result.getTotalElements());

        assertEquals(0, result.getNumber());
        assertEquals(50, result.getSize());
        assertEquals(1, result.getTotalPages());

        assertTrue(result.getContent().stream()
                .allMatch(m ->
                        !m.getMeasuredAt().isBefore(
                                OffsetDateTime.parse("2026-09-07T10:00:00Z"))
                        &&
                        !m.getMeasuredAt().isAfter(
                                OffsetDateTime.parse("2026-09-07T12:00:00Z"))));

        assertFalse(result.getContent().contains(third));
    }
}
