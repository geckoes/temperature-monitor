package dev.filippotaiuti.temperature.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class TemperatureMeasurementRepositoryTest
{
    @Autowired
    private TemperatureMeasurementRepository repository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testRejectMeasurementWithUnknownSensor() {
        // Arrange
        TemperatureMeasurement measurement = new TemperatureMeasurement(
            new BigDecimal("20.50"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-10-19T08:00:00Z"),
            "unregistered-sensor",
            null);

            // Act + Assert
            assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(measurement));
    }

    @Test
    void testFindBySensorId()
    {
        // Arrange
        OffsetDateTime registeredAt = OffsetDateTime.parse("2026-10-19T00:00:00Z");

        sensorRepository.saveAndFlush(new Sensor("sensor-001", "terminal-A", null, registeredAt));
        sensorRepository.saveAndFlush(new Sensor("sensor-002", "terminal-B", null, registeredAt));

        TemperatureMeasurement first = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.now(),
            "sensor-001",
            null);
        TemperatureMeasurement second = new TemperatureMeasurement(
            new BigDecimal("72.5"),
            TemperatureUnit.FAHRENHEIT,
            OffsetDateTime.now(),
            "sensor-001",
            null);

        TemperatureMeasurement third = new TemperatureMeasurement(
            new BigDecimal("22.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.now(),
            "sensor-002",
            null);

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
        sensorRepository.saveAndFlush(new Sensor(
            "sensor-001",
            "terminal-A",
            null,
            OffsetDateTime.parse("2026-10-19T00:00:00Z")
        ));

        TemperatureMeasurement first = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-09-07T10:00:00Z"),
            "sensor-001",
            null);
        TemperatureMeasurement second = new TemperatureMeasurement(
            new BigDecimal("72.5"),
            TemperatureUnit.FAHRENHEIT,
            OffsetDateTime.parse("2026-09-07T11:00:00Z"),
            "sensor-001",
            null);

        TemperatureMeasurement third = new TemperatureMeasurement(
            new BigDecimal("22.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-09-07T13:00:00Z"),
            "sensor-001",
            null);

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

    @Test
    void testPersistMeasurementWithTerminalId() {
        // Arrange
        sensorRepository.saveAndFlush(new Sensor(
            "sensor-001",
            "terminal-A",
            null,
            OffsetDateTime.parse("2026-10-01T00:00:00Z")));

        TemperatureMeasurement measurement = new TemperatureMeasurement(
            new BigDecimal("20.50"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-10-20T08:00:00Z"),
            "sensor-001",
            "terminal-A");

        // Act
        TemperatureMeasurement saved = repository.saveAndFlush(measurement);
        Long id = saved.getId();

        entityManager.clear();

        TemperatureMeasurement result = repository.findById(id).orElseThrow();

        // Assert
        assertEquals("terminal-A", result.getTerminalId());
    }
}
