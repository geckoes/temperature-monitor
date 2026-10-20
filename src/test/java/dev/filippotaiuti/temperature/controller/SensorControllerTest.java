package dev.filippotaiuti.temperature.controller;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.filippotaiuti.temperature.dto.SensorRegistrationRequest;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.exception.SensorAlreadyExistsException;
import dev.filippotaiuti.temperature.exception.TerminalAlreadyAssignedException;
import dev.filippotaiuti.temperature.mapper.SensorResponseMapper;
import dev.filippotaiuti.temperature.service.SensorService;

@Import (SensorResponseMapper.class)
@WebMvcTest (SensorController.class)
class SensorControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensorService service;

    @Test
    void testRegisterSensorWithoutName() throws Exception
    {
        // Arrange
        Sensor saved = new Sensor(
                "sensor-001",
                "terminal-A",
                null,
                OffsetDateTime.parse("2026-10-16T08:00:00Z"));

        when(service.register(any(SensorRegistrationRequest.class)))
                .thenReturn(saved);

        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-001",
                         "terminalId": "terminal-A"
                         }
                         """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/sensors/sensor-001"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sensorId").value("sensor-001"))
                .andExpect(jsonPath("$.terminalId").value("terminal-A"))
                .andExpect(jsonPath("$.createdAt").exists());

    }

    @Test
    void testSensorRegistrationRequest() throws Exception
    {
        // Arrange
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-16T08:00:00Z");

        Sensor saved = new Sensor(
            "sensor-002",
            "terminal-B",
            "Temperature Sensor B",
            createdAt
        );

        when(service.register(any(SensorRegistrationRequest.class)))
            .thenReturn(saved);

        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-002",
                         "terminalId": "terminal-B",
                         "name": "Temperature Sensor B"
                         }
                         """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/sensors/sensor-002"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sensorId").value("sensor-002"))
                .andExpect(jsonPath("$.terminalId").value("terminal-B"))
                .andExpect(jsonPath("$.name").value("Temperature Sensor B"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-16T08:00:00Z"));

        // Verify
        verify(service).register(new SensorRegistrationRequest(
            "sensor-002",
            "terminal-B",
            "Temperature Sensor B")
        );
    }

    @Test
    void testRejectRegistrationWithAssignedTerminal() throws Exception
    {
        // Arrange
        when(service.register(any(SensorRegistrationRequest.class)))
            .thenThrow(new TerminalAlreadyAssignedException("terminal-A"));

        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-002",
                         "terminalId": "terminal-A"
                         }
                         """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("TERMINAL_ALREADY_ASSIGNED"));
    }

    @Test
    void testRejectRegistrationWithExistingSensorId() throws Exception
    {
        // Arrange
        when(service.register(any(SensorRegistrationRequest.class)))
            .thenThrow(new SensorAlreadyExistsException("sensor-001"));

        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-001",
                         "terminalId": "terminal-B"
                         }
                         """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("SENSOR_ALREADY_EXISTS"));
    }

    @Test
    void testRejectRegistrationWithoutSensorId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "terminalId": "terminal-A"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("sensorId"))
                .andExpect(jsonPath("$.errors[0].message").value("sensorId must not be blank"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectRegistrationWithoutTerminalId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-001"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("terminalId"))
                .andExpect(jsonPath("$.errors[0].message").value("terminalId must not be blank"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectRegistrationWithBlankSensorId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "   ",
                         "terminalId": "terminal-A"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("sensorId"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectRegistrationWithBlankTerminalId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-001",
                         "terminalId": "   "
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("terminalId"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectRegistrationWithSensorIdTooLong() throws Exception
    {
        // Arrange
        String sensorId = "s".repeat(101);
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "%s",
                         "terminalId": "terminal-A"
                         }
                         """.formatted(sensorId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("sensorId"))
                .andExpect(jsonPath("$.errors[0].message").value("sensorId must not exceed 100 characters"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectRegistrationWithTerminalIdTooLong() throws Exception
    {
        // Arrange
        String terminalId = "t".repeat(101);
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-001",
                         "terminalId": "%s"
                         }
                         """.formatted(terminalId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("terminalId"))
                .andExpect(jsonPath("$.errors[0].message").value("terminalId must not exceed 100 characters"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectRegistrationWithNameTooLong() throws Exception
    {
        // Arrange
        String name = "n".repeat(256);
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-001",
                         "terminalId": "terminal-A",
                         "name": "%s"
                         }
                         """.formatted(name)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("name must not exceed 255 characters"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRegisterSensorWithMaximumFieldLengths() throws Exception
    {
        // Arrange
        String sensorId = "s".repeat(100);
        String terminalId = "t".repeat(100);
        String name = "n".repeat(255);

        Sensor saved = new Sensor(
            sensorId,
            terminalId,
            name,
            OffsetDateTime.parse("2026-10-16T08:00:00Z"));

        when(service.register(new SensorRegistrationRequest(sensorId, terminalId, name))).thenReturn(saved);
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "%s",
                         "terminalId": "%s",
                         "name": "%s"
                         }
                         """.formatted(sensorId, terminalId, name)))
                .andExpect(status().isCreated());

        // Verify
        verify(service).register(new SensorRegistrationRequest(
            sensorId,
            terminalId,
            name)
        );
    }

    @Test
    void testRejectRegistrationWithInvalidSensorId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor 001",
                         "terminalId": "terminal-A"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("sensorId"))
                .andExpect(jsonPath("$.errors[0].message").value("sensorId contains invalid characters"));


        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectRegistrationWithInvalidTerminalId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "sensor-001",
                         "terminalId": "terminal A"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("terminalId"))
                .andExpect(jsonPath("$.errors[0].message").value("terminalId contains invalid characters"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRegisterSensorWithSpecialCharacters() throws Exception
    {
        // Arrange
        String sensorId = "sensor.room_01-A";
        String terminalId = "terminal.size_02-B";

        Sensor saved = new Sensor(
            sensorId,
            terminalId,
            null,
            OffsetDateTime.parse("2026-10-16T08:00:00Z"));

        when(service.register(new SensorRegistrationRequest(sensorId, terminalId, null))).thenReturn(saved);
        // Act + Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "sensorId": "%s",
                         "terminalId": "%s"
                         }
                         """.formatted(sensorId, terminalId)))
                .andExpect(status().isCreated());

        // Verify
        verify(service).register(new SensorRegistrationRequest(
            sensorId,
            terminalId,
            null)
        );
    }

}
