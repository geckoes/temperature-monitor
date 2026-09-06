package dev.filippotaiuti.temperature.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import dev.filippotaiuti.temperature.service.TemperatureMeasurementService;

@WebMvcTest(TemperatureMeasurementController.class)
class TemperatureMeasurementControllerTest
{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemperatureMeasurementService service;

    @Test
    void testCreateMeasurement() throws Exception
    {
        TemperatureMeasurement saved = new TemperatureMeasurement(
                new BigDecimal("23.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.now(),
                "sensor-001");

        when(service.save(any(TemperatureMeasurementRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": "CELSIUS",
                         "sensorId": "sensor-001",
                         "measuredAt": "2026-09-06T15:00:00Z"
                         }
                         """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(23.5))
                .andExpect(jsonPath("$.unit").value("CELSIUS"))
                .andExpect(jsonPath("$.sensorId").value("sensor-001"))
                .andExpect(jsonPath("$.createdAt").doesNotExist());

        verify(service).save(any(TemperatureMeasurementRequest.class));
    }

    @Test
    void testRejectMeasurementWithoutTemperature() throws Exception
    {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": null,
                         "unit": "FAHRENHEIT",
                         "sensorId": "sensor-001",
                         "measuredAt": "2026-09-06T15:00:00Z"
                         }
                         """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutUnit() throws Exception
    {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": null,
                         "sensorId": "sensor-001",
                         "measuredAt": "2026-09-06T15:00:00Z"
                         }
                         """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutSensorId() throws Exception
    {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": "CELSIUS",
                         "sensorId": null,
                         "measuredAt": "2026-09-06T15:00:00Z"
                         }
                         """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithInvalidUnit() throws Exception
    {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": "CELSIUSS",
                         "sensorId": "sensor-002",
                         "measuredAt": "2026-09-06T15:00:00Z"
                         }
                         """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutMeasuredAt() throws Exception
    {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": "CELSIUS",
                         "sensorId": "sensor-002",
                         "measuredAt": null
                         }
                         """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testGetMeasurements() throws Exception
    {
        TemperatureMeasurement first = new TemperatureMeasurement(
                new BigDecimal("21.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.now(),
                "sensor-001");

        TemperatureMeasurement second = new TemperatureMeasurement(
                new BigDecimal("72.7"),
                TemperatureUnit.FAHRENHEIT,
                OffsetDateTime.now(),
                "sensor-002");

        when(service.findAll())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/measurements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].temperature").value(21.5))
                .andExpect(jsonPath("$[0].unit").value("CELSIUS"))
                .andExpect(jsonPath("$[0].sensorId").value("sensor-001"))
                .andExpect(jsonPath("$[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$[1].temperature").value(72.7))
                .andExpect(jsonPath("$[1].unit").value("FAHRENHEIT"))
                .andExpect(jsonPath("$[1].sensorId").value("sensor-002"))
                .andExpect(jsonPath("$[1].createdAt").doesNotExist());
        verify(service).findAll();
    }

    @Test
    void testGetMeasurementsBySensorId() throws Exception
    {
        // Arrange
        TemperatureMeasurement first = new TemperatureMeasurement(
                new BigDecimal("21.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.now(),
                "sensor-001");

        TemperatureMeasurement second = new TemperatureMeasurement(
                new BigDecimal("72.7"),
                TemperatureUnit.FAHRENHEIT,
                OffsetDateTime.now(),
                "sensor-001");

        when(service.findBySensorId("sensor-001"))
                .thenReturn(List.of(first, second));

        // Act + Assert
        mockMvc.perform(get("/api/measurements").param("sensorId", "sensor-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].temperature").value(21.5))
                .andExpect(jsonPath("$[0].unit").value("CELSIUS"))
                .andExpect(jsonPath("$[0].sensorId").value("sensor-001"))
                .andExpect(jsonPath("$[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$[1].temperature").value(72.7))
                .andExpect(jsonPath("$[1].unit").value("FAHRENHEIT"))
                .andExpect(jsonPath("$[1].sensorId").value("sensor-001"))
                .andExpect(jsonPath("$[1].createdAt").doesNotExist());

        verify(service).findBySensorId("sensor-001");
    }
}