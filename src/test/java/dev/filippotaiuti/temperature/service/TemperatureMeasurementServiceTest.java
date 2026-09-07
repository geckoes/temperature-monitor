package dev.filippotaiuti.temperature.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;

@ExtendWith(MockitoExtension.class)
class TemperatureMeasurementServiceTest
{
    @Mock
    private TemperatureMeasurementRepository repository;

    @InjectMocks
    private TemperatureMeasurementService service;

    @Test
    void testSaveTemperatureMeasurement()
    {
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
                BigDecimal.valueOf(25.5),
                TemperatureUnit.CELSIUS,
                "sensor-001",
                OffsetDateTime.parse("2026-09-06T15:00:00Z"));

        ArgumentCaptor<TemperatureMeasurement> captor = ArgumentCaptor.forClass(TemperatureMeasurement.class);

        service.save(request);

        verify(repository).save(captor.capture());

        TemperatureMeasurement saved = captor.getValue();

        assertNotNull(saved);
        assertEquals(request.getTemperature(), saved.getTemperature());
        assertEquals(request.getUnit(), saved.getUnit());
        assertEquals(request.getSensorId(), saved.getSensorId());
        assertNotNull(saved.getMeasuredAt());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testFindAllMeasurements()
    {
        TemperatureMeasurement first = new TemperatureMeasurement(
                new BigDecimal("21.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.now(),
                "sensor-001");
        TemperatureMeasurement second = new TemperatureMeasurement(
                new BigDecimal("72.5"),
                TemperatureUnit.FAHRENHEIT,
                OffsetDateTime.now(),
                "sensor-002");

        List<TemperatureMeasurement> measurements = List.of(first, second);

        when(repository.findAll()).thenReturn(measurements);

        List<TemperatureMeasurement> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals(measurements, result);
        verify(repository).findAll();
    }

    @Test
    void testSavePreservesMeasuredAtFromRequest()
    {
        OffsetDateTime measuredAt = OffsetDateTime.parse("2026-09-06T15:00:00Z");

        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest();

        request.setTemperature(new BigDecimal("23.5"));
        request.setUnit(TemperatureUnit.CELSIUS);
        request.setSensorId("sensor-001");
        request.setMeasuredAt(measuredAt);

        service.save(request);
    }

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

        List<TemperatureMeasurement> measurements = List.of(first, second);

        when(repository.findBySensorId("sensor-001")).thenReturn(measurements);

        // Act
        List<TemperatureMeasurement> result = service.findBySensorId("sensor-001");

        // Assert
        assertEquals(2, result.size());
        assertEquals(measurements, result);
        verify(repository).findBySensorId("sensor-001");
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

        List<TemperatureMeasurement> measurements = List.of(first, second);

        when(repository.findBySensorIdAndMeasuredAtBetween("sensor-001", OffsetDateTime.parse("2026-09-07T10:00:00Z"),
                OffsetDateTime.parse("2026-09-07T12:00:00Z"))).thenReturn(measurements);

        // Act
        List<TemperatureMeasurement> result = service.findBySensorIdAndMeasuredAtBetween("sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"), OffsetDateTime.parse("2026-09-07T12:00:00Z"));

        // Assert
        assertEquals(2, result.size());
        assertEquals(measurements, result);
        verify(repository).findBySensorIdAndMeasuredAtBetween("sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"), OffsetDateTime.parse("2026-09-07T12:00:00Z"));
    }
}