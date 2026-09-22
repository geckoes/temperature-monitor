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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

        Pageable expectedPageable = PageRequest.of(
            0,
            50,
            Sort.by(
                    Sort.Order.desc("measuredAt"),
                    Sort.Order.asc("sensorId")
            )
        );
        
        Page<TemperatureMeasurement> measurementsPage =
            new PageImpl<>(measurements, expectedPageable, measurements.size());

        when(repository.findAll(expectedPageable)).thenReturn(measurementsPage);


        Page<TemperatureMeasurement> result = service.findAll(expectedPageable);

        assertEquals(measurementsPage.getContent(), result.getContent());
        assertEquals(measurementsPage.getNumber(), result.getNumber());
        assertEquals(measurementsPage.getSize(), result.getSize());
        assertEquals(measurementsPage.getNumberOfElements(), result.getNumberOfElements());
        assertEquals(measurementsPage.getTotalElements(), result.getTotalElements());
        assertEquals(measurementsPage.getTotalPages(), result.getTotalPages());

        verify(repository).findAll(expectedPageable);
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

        Pageable pageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")
                )
        );
        Page<TemperatureMeasurement> measurementsPage =
                new PageImpl<>(
                        measurements,
                        pageable,
                        measurements.size()
        );
        when(repository.findBySensorId("sensor-001", pageable)).thenReturn(measurementsPage);

        // Act
        Page<TemperatureMeasurement> result = service.findBySensorId("sensor-001", pageable);

        // Assert
        assertEquals(measurementsPage.getContent(), result.getContent());
        assertEquals(measurementsPage.getTotalElements(), result.getTotalElements());

        verify(repository).findBySensorId("sensor-001", pageable);
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

        Pageable pageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")
                )
        );
        Page<TemperatureMeasurement> measurementsPage =
                new PageImpl<>(
                        measurements,
                        pageable,
                        measurements.size()
        );

        when(repository.findBySensorIdAndMeasuredAtBetween("sensor-001", OffsetDateTime.parse("2026-09-07T10:00:00Z"),
                OffsetDateTime.parse("2026-09-07T12:00:00Z"), pageable)).thenReturn(measurementsPage);

        // Act
        Page<TemperatureMeasurement> result = service.findBySensorIdAndMeasuredAtBetween("sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"), OffsetDateTime.parse("2026-09-07T12:00:00Z"), pageable);

        // Assert
        assertEquals(measurementsPage.getSize(), result.getSize());
        assertEquals(measurementsPage.getContent(), result.getContent());
        verify(repository).findBySensorIdAndMeasuredAtBetween("sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"), OffsetDateTime.parse("2026-09-07T12:00:00Z"), pageable);
    }
}