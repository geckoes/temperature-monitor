package dev.filippotaiuti.temperature.repository;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import dev.filippotaiuti.temperature.entity.Sensor;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class SensorRepositoryTest {

    @Autowired
    private SensorRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testFindById() {
        // Arrange
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-16T08:00:00Z");

        Sensor sensor = new Sensor(
            "sensor-001", 
            "terminal-A", 
            null,
            createdAt
        );

        // Act
        repository.save(sensor);
        entityManager.flush();
        entityManager.clear();

        Sensor result = repository.findById("sensor-001").orElseThrow();

        // Assert
        assertNotNull(result);
        assertEquals("sensor-001", result.getSensorId());
        assertEquals("terminal-A", result.getTerminalId());
        assertNull(result.getName());
        assertEquals(createdAt.toInstant(), result.getCreatedAt().toInstant());
    }

    @Test
    void testRejectSensorsWithSameTerminalId() {
        // Arrange
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-16T08:00:00Z");

        repository.saveAndFlush(new Sensor(
            "sensor-001", 
            "terminal-A", 
            null,
            createdAt
        ));


        // Act + Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.saveAndFlush(new Sensor(
                "sensor-002", 
                "terminal-A", 
                null,
                createdAt
            ));
        });
    }
}
