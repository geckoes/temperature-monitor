package dev.filippotaiuti.temperature.service;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.filippotaiuti.temperature.dto.SensorRegistrationRequest;
import dev.filippotaiuti.temperature.dto.SensorTerminalAssignmentRequest;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.exception.TerminalAlreadyAssignedException;
import dev.filippotaiuti.temperature.exception.SensorAlreadyExistsException;
import dev.filippotaiuti.temperature.exception.SensorNotFoundException;
import dev.filippotaiuti.temperature.repository.SensorRepository;

@ExtendWith (MockitoExtension.class)
public class SensorServiceTest {

    @Mock
    private SensorRepository repository;

    @InjectMocks
    private SensorService service;

    @Test
    void testRegisterSensorReturnsSavedSensor() {
        // Arrange
        SensorRegistrationRequest request = new SensorRegistrationRequest(
            "sensor-001",
            "terminal-A",
            "North Warehouse"
        );

        Sensor sensor = new Sensor(
            "sensor-001",
            "terminal-A",
            "North Warehouse",
            OffsetDateTime.parse("2026-10-16T00:00:00Z")
        );

        when(repository.save(any(Sensor.class))).thenReturn(sensor);

        // Act
        Sensor result = service.register(request);

        // Assert
        assertSame(sensor, result);

        ArgumentCaptor<Sensor> captor = ArgumentCaptor.forClass(Sensor.class);

        // Verify
        verify(repository).save(captor.capture());

        Sensor submitted = captor.getValue();

        // Assert
        assertEquals(sensor.getSensorId(), submitted.getSensorId());
        assertEquals(sensor.getTerminalId(), submitted.getTerminalId());
        assertEquals(sensor.getName(), submitted.getName());
        assertNotNull(submitted.getCreatedAt());
    }

    @Test
    void testRejectRegistrationWithAssignedTerminal() {
        // Arrange
        SensorRegistrationRequest request = new SensorRegistrationRequest(
            "sensor-002",
            "terminal-A",
            null
        );

        when(repository.existsByTerminalId("terminal-A")).thenReturn(true);

        // Act + Assert
        assertThrows(TerminalAlreadyAssignedException.class, () -> {
            service.register(request);
        });

        // Verify
        verify(repository, never()).save(any(Sensor.class));
    }

    @Test
    void testRejectRegistrationWithExistingSensorId() {
        // Arrange
        SensorRegistrationRequest request = new SensorRegistrationRequest(
            "sensor-001",
            "terminal-B",
            null
        );

        when(repository.existsById("sensor-001")).thenReturn(true);

        // Act + Assert
        assertThrows(SensorAlreadyExistsException.class, () -> {
            service.register(request);
        });

        // Verify
        verify(repository, never()).save(any(Sensor.class));
    }

    @Test
    void testFindSensorById() {
        // Arrange
        Sensor sensor = new Sensor(
            "sensor-001",
            "terminal-A",
            "North Warehouse",
            OffsetDateTime.parse("2026-10-16T00:00:00Z")
        );

        when(repository.findById("sensor-001")).thenReturn(Optional.of(sensor));

        // Act
        Sensor result = service.fetchSensorById("sensor-001");

        // Assert
        assertSame(sensor, result);

        // Verify
        verify(repository).findById("sensor-001");
    }

    @Test
    void testFetchUnknownThrowsSensorNotFound() {
        // Arrange
        when(repository.findById("sensor-unknown")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(SensorNotFoundException.class, () -> {
            service.fetchSensorById("sensor-unknown");
        });
    }

    @Test
    void testAssignTerminalToSensor() {
        // Arrange
        Sensor sensor = new Sensor(
            "sensor-001",
            "terminal-A",
            "North Warehouse",
            OffsetDateTime.parse("2026-10-16T00:00:00Z")
        );

        when(repository.findById("sensor-001"))
            .thenReturn(Optional.of(sensor));
        
        when(repository.save(any(Sensor.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Sensor result = service.assignTerminalToSensor("sensor-001", new SensorTerminalAssignmentRequest("terminal-B"));
        ArgumentCaptor<Sensor> captor = ArgumentCaptor.forClass(Sensor.class);
        
        // Verify
        verify(repository).save(captor.capture());
        
        // Assert
        Sensor submitted = captor.getValue();
        assertEquals("sensor-001", submitted.getSensorId());
        assertEquals("terminal-B", submitted.getTerminalId());
        assertEquals("North Warehouse", submitted.getName());
        assertEquals(OffsetDateTime.parse("2026-10-16T00:00:00Z"), submitted.getCreatedAt());
        assertSame(submitted, result);
    }    

    @Test
    void testAssignTerminalToSensorWhenTerminalIsNull() {
        // Arrange
        Sensor sensor = new Sensor(
            "sensor-001",
            null,
            "North Warehouse",
            OffsetDateTime.parse("2026-10-16T00:00:00Z")
        );

        when(repository.findById("sensor-001"))
            .thenReturn(Optional.of(sensor));
        
        when(repository.save(any(Sensor.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Sensor result = service.assignTerminalToSensor("sensor-001", new SensorTerminalAssignmentRequest("terminal-B"));
        ArgumentCaptor<Sensor> captor = ArgumentCaptor.forClass(Sensor.class);
        
        // Verify
        verify(repository).save(captor.capture());
        
        // Assert
        Sensor submitted = captor.getValue();
        assertEquals("sensor-001", submitted.getSensorId());
        assertEquals("terminal-B", submitted.getTerminalId());
        assertEquals("North Warehouse", submitted.getName());
        assertEquals(OffsetDateTime.parse("2026-10-16T00:00:00Z"), submitted.getCreatedAt());
        assertSame(submitted, result);
    }    

    @Test
    void testTerminalHasAlreadyBeenAssigned() {
        // Arrange
        Sensor sensor = new Sensor(
            "sensor-001",
            "terminal-A",
            "North Warehouse",
            OffsetDateTime.parse("2026-10-16T00:00:00Z")
        );

        when(repository.findById("sensor-001"))
        .thenReturn(Optional.of(sensor));
        
        when(repository.existsByTerminalId("terminal-B"))
            .thenReturn(true);

        // Act + Assert
        assertThrows(TerminalAlreadyAssignedException.class,
            () -> service.assignTerminalToSensor(
                "sensor-001",
                new SensorTerminalAssignmentRequest("terminal-B")));
            
        // Verify
        verify(repository, never()).save(any(Sensor.class));
    }

    @Test
    void testAssignTerminalToTheSameSensor() {
        // Arrange
        Sensor sensor = new Sensor(
            "sensor-001",
            "terminal-A",
            "North Warehouse",
            OffsetDateTime.parse("2026-10-16T00:00:00Z")
        );

        when(repository.findById("sensor-001"))
            .thenReturn(Optional.of(sensor));
        
        when(repository.save(any(Sensor.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Sensor result = service.assignTerminalToSensor("sensor-001", new SensorTerminalAssignmentRequest("terminal-A"));
        ArgumentCaptor<Sensor> captor = ArgumentCaptor.forClass(Sensor.class);
        
        // Verify
        verify(repository).save(captor.capture());
        
        // Assert
        Sensor submitted = captor.getValue();
        assertEquals("sensor-001", submitted.getSensorId());
        assertEquals("terminal-A", submitted.getTerminalId());
        assertEquals("North Warehouse", submitted.getName());
        assertEquals(OffsetDateTime.parse("2026-10-16T00:00:00Z"), submitted.getCreatedAt());
        assertSame(submitted, result);
    }    

    @Test
    void testAssignTerminalToUnknownSensor() {
        // Arrange
        when(repository.findById("sensor-unknown"))
            .thenReturn(Optional.empty());
        
        // Act + Assert
        assertThrows(SensorNotFoundException.class, () -> service.assignTerminalToSensor("sensor-unknown", new SensorTerminalAssignmentRequest("terminal-B")));
        
        // Verify
        verify(repository, never()).save(any(Sensor.class));
    }    

}
