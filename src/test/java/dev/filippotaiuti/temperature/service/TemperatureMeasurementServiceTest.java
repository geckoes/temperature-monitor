package dev.filippotaiuti.temperature.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import dev.filippotaiuti.temperature.dto.MeasurementIngestionResult;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.MeasurementIngestionStatus;
import dev.filippotaiuti.temperature.entity.QuarantinedMeasurement;
import dev.filippotaiuti.temperature.entity.QuarantinedReason;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import dev.filippotaiuti.temperature.repository.QuarantinedMeasurementRepository;
import dev.filippotaiuti.temperature.repository.SensorRepository;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;
;

@ExtendWith(MockitoExtension.class)
class TemperatureMeasurementServiceTest
{

    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private TemperatureMeasurementRepository repository;
    @Mock
    private QuarantinedMeasurementRepository quarantinedRepository;

    @InjectMocks
    private TemperatureMeasurementService service;

    @Test
    void testSaveTemperatureMeasurement()
    {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
                BigDecimal.valueOf(25.5),
                TemperatureUnit.CELSIUS,
                "sensor-001",
                "terminal-A",
                OffsetDateTime.parse("2026-09-06T15:00:00Z"));

        ArgumentCaptor<TemperatureMeasurement> captor = ArgumentCaptor.forClass(TemperatureMeasurement.class);

        Sensor sensor = new Sensor(
            "sensor-001",
            "terminal-A",
            null,
            OffsetDateTime.parse("2026-10-01T00:00:00Z"));

        when(sensorRepository.findById("sensor-001")).thenReturn(Optional.of(sensor));
        when(repository.save(any(TemperatureMeasurement.class)))
            .thenAnswer(invocation ->
                invocation.getArgument(0, TemperatureMeasurement.class));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Verify
        verify(repository).save(captor.capture());
        verify(sensorRepository).findById("sensor-001");

        TemperatureMeasurement saved = captor.getValue();

        // Assert
        assertEquals(MeasurementIngestionStatus.STORED, result.status());
        assertSame(saved, result.measurement());
        assertEquals(request.getTemperature(), saved.getTemperature());
        assertEquals(request.getUnit(), saved.getUnit());
        assertEquals(request.getSensorId(), saved.getSensorId());
        assertEquals("terminal-A", saved.getTerminalId());
        assertNotNull(saved.getMeasuredAt());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testFindAllMeasurements()
    {
        // Arrange
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
            "sensor-002",
            null);

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


        // Act
        Page<TemperatureMeasurement> result = service.findAll(expectedPageable);

        // Assert
        assertEquals(measurementsPage.getContent(), result.getContent());
        assertEquals(measurementsPage.getNumber(), result.getNumber());
        assertEquals(measurementsPage.getSize(), result.getSize());
        assertEquals(measurementsPage.getNumberOfElements(), result.getNumberOfElements());
        assertEquals(measurementsPage.getTotalElements(), result.getTotalElements());
        assertEquals(measurementsPage.getTotalPages(), result.getTotalPages());

        // Verify
        verify(repository).findAll(expectedPageable);
    }

    @Test
    void testSavePreservesMeasuredAtFromRequest()
    {
        // Arrange
        OffsetDateTime measuredAt = OffsetDateTime.parse("2026-09-06T15:00:00Z");

        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest();

        request.setTemperature(new BigDecimal("23.5"));
        request.setUnit(TemperatureUnit.CELSIUS);
        request.setSensorId("sensor-001");
        request.setMeasuredAt(measuredAt);

        // Act
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
            "sensor-001",
            null);
        TemperatureMeasurement second = new TemperatureMeasurement(
            new BigDecimal("72.5"),
            TemperatureUnit.FAHRENHEIT,
            OffsetDateTime.now(),
            "sensor-001",
            null);

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

        // Verify
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
            "sensor-001",
            null);
        TemperatureMeasurement second = new TemperatureMeasurement(
            new BigDecimal("72.5"),
            TemperatureUnit.FAHRENHEIT,
            OffsetDateTime.parse("2026-09-07T11:00:00Z"),
            "sensor-001",
            null);

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
        // Verify
        verify(repository).findBySensorIdAndMeasuredAtBetween("sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"), OffsetDateTime.parse("2026-09-07T12:00:00Z"), pageable);
    }

    @Test
    void testQuarantinedMeasurementWithUnknownSensor() {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor_unknown",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T08:00:00Z")
        );

        when(sensorRepository.findById("sensor_unknown")).thenReturn(Optional.empty());

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.QUARANTINED, result.status());
        assertEquals(QuarantinedReason.UNKNOWN_SENSOR, result.reason());
        assertNull(result.measurement());

        // Verify
        verify(repository, never()).save(any(TemperatureMeasurement.class));

        ArgumentCaptor<QuarantinedMeasurement> captor =
        ArgumentCaptor.forClass(QuarantinedMeasurement.class);

        verify(quarantinedRepository).save(captor.capture());

        QuarantinedMeasurement quarantined = captor.getValue();

        assertEquals(request.getSensorId(), quarantined.getSensorId());
        assertEquals(request.getTerminalId(), quarantined.getTerminalId());
        assertEquals(request.getTemperature(), quarantined.getTemperature());
        assertEquals(request.getUnit(), quarantined.getUnit());
        assertEquals(request.getMeasuredAt(), quarantined.getMeasuredAt());
        assertEquals(QuarantinedReason.UNKNOWN_SENSOR, quarantined.getReason());
        assertNotNull(quarantined.getReceivedAt());
    }

    @Test
    void testQuarantinedMeasurementWithTerminalMismatch() {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor-001",
            "terminal-B",
            OffsetDateTime.parse("2026-10-21T08:00:00Z")
        );

        Sensor sensor = new Sensor(
            "sensor-001",
            "terminal-A",
            null,
            OffsetDateTime.parse("2026-10-01T00:00:00Z"));

        when(sensorRepository.findById("sensor-001")).thenReturn(Optional.of(sensor));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.QUARANTINED, result.status());
        assertEquals(QuarantinedReason.TERMINAL_MISMATCH, result.reason());
        assertNull(result.measurement());

        // Verify
        verify(repository, never()).save(any(TemperatureMeasurement.class));

        ArgumentCaptor<QuarantinedMeasurement> captor =
        ArgumentCaptor.forClass(QuarantinedMeasurement.class);

        verify(quarantinedRepository).save(captor.capture());

        QuarantinedMeasurement quarantined = captor.getValue();

        assertEquals(request.getSensorId(), quarantined.getSensorId());
        assertEquals(request.getTerminalId(), quarantined.getTerminalId());
        assertEquals(request.getTemperature(), quarantined.getTemperature());
        assertEquals(request.getUnit(), quarantined.getUnit());
        assertEquals(request.getMeasuredAt(), quarantined.getMeasuredAt());
        assertEquals(QuarantinedReason.TERMINAL_MISMATCH, quarantined.getReason());
        assertNotNull(quarantined.getReceivedAt());
    }

    @Test
    void testQuarantinedMeasurementWithoutAssignedTerminal() {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor-001",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T08:00:00Z")
        );

        Sensor sensor = new Sensor(
            "sensor-001",
            null,
            null,
            OffsetDateTime.parse("2026-10-01T00:00:00Z"));

        when(sensorRepository.findById("sensor-001")).thenReturn(Optional.of(sensor));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.QUARANTINED, result.status());
        assertEquals(QuarantinedReason.TERMINAL_NOT_ASSIGNED, result.reason());
        assertNull(result.measurement());

        // Verify
        verify(repository, never()).save(any(TemperatureMeasurement.class));

        ArgumentCaptor<QuarantinedMeasurement> captor =
        ArgumentCaptor.forClass(QuarantinedMeasurement.class);

        verify(quarantinedRepository).save(captor.capture());

        QuarantinedMeasurement quarantined = captor.getValue();

        assertEquals(request.getSensorId(), quarantined.getSensorId());
        assertEquals(request.getTerminalId(), quarantined.getTerminalId());
        assertEquals(request.getTemperature(), quarantined.getTemperature());
        assertEquals(request.getUnit(), quarantined.getUnit());
        assertEquals(request.getMeasuredAt(), quarantined.getMeasuredAt());
        assertEquals(QuarantinedReason.TERMINAL_NOT_ASSIGNED, quarantined.getReason());
        assertNotNull(quarantined.getReceivedAt());
    }

    @Test
    void testReturnAlreadyStoredForIdenticalMeasurement() {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor-001",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T08:00:00Z")
        );

        TemperatureMeasurement existing = new TemperatureMeasurement(
            request.getTemperature(),
            request.getUnit(),
            request.getMeasuredAt(),
            request.getSensorId(),
            request.getTerminalId()
        );

        when(repository.findBySensorIdAndMeasuredAt(
            existing.getSensorId(),
            existing.getMeasuredAt())
        ).thenReturn(Optional.of(existing));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.ALREADY_STORED, result.status());
        assertSame(existing, result.measurement());
        assertNull(result.reason());

        // Verify
        verify(repository).findBySensorIdAndMeasuredAt(
            request.getSensorId(),
            request.getMeasuredAt()
        );

        verify(repository, never()).save(any(TemperatureMeasurement.class));
        verifyNoInteractions(sensorRepository, quarantinedRepository);
    }

    @Test
    void testQuarantinedMeasurementWithMeasurementConflict() {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("24.0"),
            TemperatureUnit.CELSIUS,
            "sensor-001",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T08:00:00Z")
        );

        TemperatureMeasurement existing = new TemperatureMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-10-21T08:00:00Z"),
            "sensor-001",
            "terminal-A"
        );

        when(repository.findBySensorIdAndMeasuredAt(
            existing.getSensorId(),
            existing.getMeasuredAt())
        ).thenReturn(Optional.of(existing));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.QUARANTINED, result.status());
        assertEquals(QuarantinedReason.MEASUREMENT_CONFLICT, result.reason());
        assertNull(result.measurement());

        // Verify
        verify(repository, never()).save(any(TemperatureMeasurement.class));
        verifyNoInteractions(sensorRepository);

        ArgumentCaptor<QuarantinedMeasurement> captor =
        ArgumentCaptor.forClass(QuarantinedMeasurement.class);

        verify(quarantinedRepository).save(captor.capture());

        QuarantinedMeasurement quarantined = captor.getValue();

        assertEquals(request.getSensorId(), quarantined.getSensorId());
        assertEquals(request.getTerminalId(), quarantined.getTerminalId());
        assertEquals(request.getTemperature(), quarantined.getTemperature());
        assertEquals(request.getUnit(), quarantined.getUnit());
        assertEquals(request.getMeasuredAt(), quarantined.getMeasuredAt());
        assertEquals(QuarantinedReason.MEASUREMENT_CONFLICT, quarantined.getReason());
        assertNotNull(quarantined.getReceivedAt());
    }

    @Test
    void testQuarantinedMeasurementWithConflictingTerminal() {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor-001",
            "terminal-B",
            OffsetDateTime.parse("2026-10-21T08:00:00Z")
        );

        TemperatureMeasurement existing = new TemperatureMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-10-21T08:00:00Z"),
            "sensor-001",
            "terminal-A"
        );

        when(repository.findBySensorIdAndMeasuredAt(
            existing.getSensorId(),
            existing.getMeasuredAt())
        ).thenReturn(Optional.of(existing));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.QUARANTINED, result.status());
        assertEquals(QuarantinedReason.MEASUREMENT_CONFLICT, result.reason());
        assertNull(result.measurement());

        // Verify
        verify(repository, never()).save(any(TemperatureMeasurement.class));
        verifyNoInteractions(sensorRepository);

        ArgumentCaptor<QuarantinedMeasurement> captor =
        ArgumentCaptor.forClass(QuarantinedMeasurement.class);

        verify(quarantinedRepository).save(captor.capture());

        QuarantinedMeasurement quarantined = captor.getValue();

        assertEquals(request.getSensorId(), quarantined.getSensorId());
        assertEquals(request.getTerminalId(), quarantined.getTerminalId());
        assertEquals(request.getTemperature(), quarantined.getTemperature());
        assertEquals(request.getUnit(), quarantined.getUnit());
        assertEquals(request.getMeasuredAt(), quarantined.getMeasuredAt());
        assertEquals(QuarantinedReason.MEASUREMENT_CONFLICT, quarantined.getReason());
        assertNotNull(quarantined.getReceivedAt());
    }

    @Test
    void testQuarantinedMeasurementWithConflictingUnit() {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("23.5"),
            TemperatureUnit.FAHRENHEIT,
            "sensor-001",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T08:00:00Z")
        );

        TemperatureMeasurement existing = new TemperatureMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-10-21T08:00:00Z"),
            "sensor-001",
            "terminal-A"
        );

        when(repository.findBySensorIdAndMeasuredAt(
            existing.getSensorId(),
            existing.getMeasuredAt())
        ).thenReturn(Optional.of(existing));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.QUARANTINED, result.status());
        assertEquals(QuarantinedReason.MEASUREMENT_CONFLICT, result.reason());
        assertNull(result.measurement());

        // Verify
        verify(repository, never()).save(any(TemperatureMeasurement.class));
        verifyNoInteractions(sensorRepository);

        ArgumentCaptor<QuarantinedMeasurement> captor =
        ArgumentCaptor.forClass(QuarantinedMeasurement.class);

        verify(quarantinedRepository).save(captor.capture());

        QuarantinedMeasurement quarantined = captor.getValue();

        assertEquals(request.getSensorId(), quarantined.getSensorId());
        assertEquals(request.getTerminalId(), quarantined.getTerminalId());
        assertEquals(request.getTemperature(), quarantined.getTemperature());
        assertEquals(request.getUnit(), quarantined.getUnit());
        assertEquals(request.getMeasuredAt(), quarantined.getMeasuredAt());
        assertEquals(QuarantinedReason.MEASUREMENT_CONFLICT, quarantined.getReason());
        assertNotNull(quarantined.getReceivedAt());
    }

    @Test
    void testReuseExistingQuarantineForIdenticalMeasurement() {
        // Arrange
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor-001",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T08:00:00Z")
        );

        QuarantinedMeasurement existing = new QuarantinedMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            "sensor-001",
            "terminal-A",
            OffsetDateTime.parse("2026-10-21T08:00:00Z"),
            QuarantinedReason.UNKNOWN_SENSOR,
            OffsetDateTime.parse("2026-10-21T08:10:00Z")
        );
        when(quarantinedRepository.findBySensorIdAndTerminalIdAndMeasuredAtAndTemperatureAndUnit(
                request.getSensorId(),
                request.getTerminalId(),
                request.getMeasuredAt(),
                request.getTemperature(),
                request.getUnit()))
        .thenReturn(Optional.of(existing));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.QUARANTINED, result.status());
        assertEquals(existing.getReason(), result.reason());
        assertNull(result.measurement());

        // Verify
        verify(repository, never()).save(any(TemperatureMeasurement.class));
        verify(quarantinedRepository, never()).save(any(QuarantinedMeasurement.class));
        verifyNoInteractions(sensorRepository);
    }

    @Test
    void testReuseExistingQuarantineForConflictingMeasurement() {
        // Arrange
        OffsetDateTime measuredAt =
                OffsetDateTime.parse("2026-10-28T08:00:00Z");

        TemperatureMeasurementRequest request =
                new TemperatureMeasurementRequest(
                        new BigDecimal("24.0"),
                        TemperatureUnit.CELSIUS,
                        "sensor-001",
                        "terminal-A",
                        measuredAt);

        TemperatureMeasurement stored = new TemperatureMeasurement(
                new BigDecimal("23.5"),
                TemperatureUnit.CELSIUS,
                measuredAt,
                "sensor-001",
                "terminal-A");

        QuarantinedMeasurement existingQuarantine =
                new QuarantinedMeasurement(
                        request.getTemperature(),
                        request.getUnit(),
                        request.getSensorId(),
                        request.getTerminalId(),
                        measuredAt,
                        QuarantinedReason.MEASUREMENT_CONFLICT,
                        OffsetDateTime.parse("2026-10-28T08:01:00Z"));

        when(repository.findBySensorIdAndMeasuredAt(
                request.getSensorId(), measuredAt))
                .thenReturn(Optional.of(stored));

        when(quarantinedRepository
                .findBySensorIdAndTerminalIdAndMeasuredAtAndTemperatureAndUnit(
                        request.getSensorId(),
                        request.getTerminalId(),
                        measuredAt,
                        request.getTemperature(),
                        request.getUnit()))
                .thenReturn(Optional.of(existingQuarantine));

        // Act
        MeasurementIngestionResult result = service.save(request);

        // Assert
        assertEquals(MeasurementIngestionStatus.QUARANTINED, result.status());
        assertEquals(QuarantinedReason.MEASUREMENT_CONFLICT, result.reason());
        assertNull(result.measurement());

        // Verify
        verify(quarantinedRepository)
                .findBySensorIdAndTerminalIdAndMeasuredAtAndTemperatureAndUnit(
                        request.getSensorId(),
                        request.getTerminalId(),
                        measuredAt,
                        request.getTemperature(),
                        request.getUnit());

        verify(repository, never()).save(any(TemperatureMeasurement.class));
        verify(quarantinedRepository, never())
                .save(any(QuarantinedMeasurement.class));
        verifyNoInteractions(sensorRepository);
    }

}
