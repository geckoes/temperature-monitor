package dev.filippotaiuti.temperature.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import dev.filippotaiuti.temperature.service.TemperatureMeasurementService;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;

@WebMvcTest(TemperatureMeasurementController.class)
class TemperatureMeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemperatureMeasurementService service;

    @Test
    void testCreateMeasurement() throws Exception {
        TemperatureMeasurement saved = new TemperatureMeasurement(
                new BigDecimal("23.5"),
                TemperatureUnit.CELSIUS,
                OffsetDateTime.now(),
                "sensor-001"
        );

        when(service.save(any(TemperatureMeasurementRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "temperature": 23.5,
                                "unit": "CELSIUS",
                                "sensorId": "sensor-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(23.5))
                .andExpect(jsonPath("$.unit").value("CELSIUS"))
                .andExpect(jsonPath("$.sensorId").value("sensor-001"));

        verify(service).save(any(TemperatureMeasurementRequest.class));
    }

    @Test
    void testRejectMeasurementWithoutTemperature() throws Exception {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "temperature": null,                        
                        "unit": "FAHRENHEIT",
                        "sensorId": "sensor-001"
                        }
                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutUnit() throws Exception {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "temperature": 23.5,                        
                        "unit": null,
                        "sensorId": "sensor-001"
                        }
                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutSensorId() throws Exception {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "temperature": 23.5,                        
                        "unit": "CELSIUS",
                        "sensorId": null
                        }
                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithInvalidUnit() throws Exception {

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "temperature": 23.5,                        
                        "unit": "CELSIUSS",
                        "sensorId": "sensor-002"
                        }
                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}