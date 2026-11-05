package dev.filippotaiuti.temperature.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.filippotaiuti.temperature.dto.SensorTerminalAssignmentRequest;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import dev.filippotaiuti.temperature.repository.SensorRepository;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;


@SpringBootTest 
@ActiveProfiles("local")
@Transactional 
public class SensorServiceIntegrationTest {

    @Autowired
    private SensorRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private TemperatureMeasurementRepository measurementRepository;

    @Test
    void testUpdateSensorsWithNewerTerminalId() {
        // Arrange
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-11-05T08:00:00Z");

        repository.save(new Sensor(
            "sensor-001", 
            "terminal-A", 
            "North Warehouse",
            createdAt
        ));
        entityManager.flush();
        entityManager.clear();

        Sensor sensor = sensorService.assignTerminalToSensor(
            "sensor-001",
            new SensorTerminalAssignmentRequest("terminal-B")
        );
        entityManager.flush();
        entityManager.clear();

        Sensor updatedSensor = repository.findById("sensor-001").orElseThrow();
        assertEquals("terminal-B", updatedSensor.getTerminalId());
        assertEquals("sensor-001", updatedSensor.getSensorId());
        assertEquals(createdAt, updatedSensor.getCreatedAt());

    }

    @Test
    void testTerminalReplacementPreservesMeasurementHistory() {
        // Arrange
        String sensorId = "sensor-history";
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-11-05T08:00:00Z");
        OffsetDateTime measuredAt = OffsetDateTime.parse("2026-11-05T09:00:00Z");

        repository.save(new Sensor(
            sensorId,
            "terminal-history-A",
            "North Warehouse",
            createdAt));

        TemperatureMeasurement measurement = measurementRepository.save(
            new TemperatureMeasurement(
                new BigDecimal("23.50"),
                TemperatureUnit.CELSIUS,
                measuredAt,
                sensorId,
                "terminal-history-A"));

        Long measurementId = measurement.getId();

        entityManager.flush();
        entityManager.clear();

        // Act
        sensorService.assignTerminalToSensor(
            sensorId,
            new SensorTerminalAssignmentRequest("terminal-history-B"));

        entityManager.flush();
        entityManager.clear();

        // Assert
        Sensor updatedSensor = repository.findById(sensorId).orElseThrow();
        TemperatureMeasurement historicalMeasurement = measurementRepository
            .findById(measurementId)
            .orElseThrow();

        assertEquals("terminal-history-B", updatedSensor.getTerminalId());
        assertEquals("North Warehouse", updatedSensor.getName());
        assertEquals(createdAt, updatedSensor.getCreatedAt());

        assertEquals(sensorId, historicalMeasurement.getSensorId());
        assertEquals("terminal-history-A", historicalMeasurement.getTerminalId());
        assertEquals(measuredAt, historicalMeasurement.getMeasuredAt());
        assertEquals(
            0,
            new BigDecimal("23.50").compareTo(historicalMeasurement.getTemperature()));
        assertEquals(TemperatureUnit.CELSIUS, historicalMeasurement.getUnit());
    }
}
