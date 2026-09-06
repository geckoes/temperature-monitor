package dev.filippotaiuti.temperature.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
