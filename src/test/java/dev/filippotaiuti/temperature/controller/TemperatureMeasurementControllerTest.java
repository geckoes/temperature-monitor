package dev.filippotaiuti.temperature.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.filippotaiuti.temperature.dto.MeasurementIngestionResult;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.MeasurementIngestionStatus;
import dev.filippotaiuti.temperature.entity.QuarantinedReason;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import dev.filippotaiuti.temperature.mapper.TemperatureMeasurementResponseMapper;
import dev.filippotaiuti.temperature.service.TemperatureMeasurementService;
import dev.filippotaiuti.temperature.validation.PaginationParameterValidator;

@Import({
    TemperatureMeasurementResponseMapper.class,
    PaginationParameterValidator.class
})
@WebMvcTest(TemperatureMeasurementController.class)
class TemperatureMeasurementControllerTest
{
    private static final int MAX_PAGE_SIZE = 1000;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemperatureMeasurementService service;

    @Test
    void testCreateMeasurement() throws Exception
    {
        // Arrange
        TemperatureMeasurement saved = new TemperatureMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.now(),
            "sensor-001",
            "terminal-A");

        when(service.save(any(TemperatureMeasurementRequest.class)))
            .thenReturn(new MeasurementIngestionResult(
                MeasurementIngestionStatus.STORED,
                saved,
                null
            ));

        // Act + Assert
        mockMvc.perform(post("/api/measurements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                        {
                        "temperature": 23.5,
                        "unit": "CELSIUS",
                        "sensorId": "sensor-001",
                        "terminalId": "terminal-A",
                        "measuredAt": "2026-09-06T15:00:00Z"
                        }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.temperature").value(23.5))
            .andExpect(jsonPath("$.unit").value("CELSIUS"))
            .andExpect(jsonPath("$.sensorId").value("sensor-001"))
            .andExpect(jsonPath("$.terminalId").value("terminal-A"))
            .andExpect(jsonPath("$.createdAt").doesNotExist());

        // Verify
        ArgumentCaptor<TemperatureMeasurementRequest> captor = ArgumentCaptor.forClass(TemperatureMeasurementRequest.class);

        verify(service).save(captor.capture());

        TemperatureMeasurementRequest received = captor.getValue();
        // Assert
        assertEquals("terminal-A", received.getTerminalId());
    }

    @Test
    void testRejectMeasurementWithoutTemperature() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                    "temperature": null,
                    "unit": "FAHRENHEIT",
                    "sensorId": "sensor-001",
                    "terminalId": "terminal-A",
                    "measuredAt": "2026-09-06T15:00:00Z"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.errors[0].field").value("temperature"))
            .andExpect(jsonPath("$.errors[0].message").value("temperature is required"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutUnit() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                    "temperature": 23.5,
                    "unit": null,
                    "sensorId": "sensor-001",
                    "terminalId": "terminal-A",
                    "measuredAt": "2026-09-06T15:00:00Z"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.errors[0].field").value("unit"))
            .andExpect(jsonPath("$.errors[0].message").value("unit is required"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutSensorId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": "CELSIUS",
                         "sensorId": null,
                         "terminalId": "terminal-A",
                         "measuredAt": "2026-09-06T15:00:00Z"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("sensorId"))
                .andExpect(jsonPath("$.errors[0].message").value("sensorId must not be blank"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutTemperatureAndSensorId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "unit": "CELSIUS",
                         "measuredAt": "2026-09-06T15:00:00Z",
                         "terminalId": "terminal-A"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[?(@.field == 'temperature')].message", contains("temperature is required")))
                .andExpect(jsonPath("$.errors[?(@.field == 'sensorId')].message", contains("sensorId must not be blank")));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithInvalidUnit() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": "KELVIN",
                         "sensorId": "sensor-002",
                         "measuredAt": "2026-09-06T15:00:00Z"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.detail").value("Request body is malformed or contains invalid values"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectIncompleteBody() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"temperature\":"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.detail").value("Request body is malformed or contains invalid values"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementInvalidTemperature() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": "caldo",
                         "unit": "FAHRENHEIT",
                         "sensorId": "sensor-001",
                         "measuredAt": "2026-09-06T15:00:00Z"
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.detail").value("Request body is malformed or contains invalid values"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutMeasuredAt() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": "CELSIUS",
                         "sensorId": "sensor-002",
                         "terminalId": "terminal-A",
                         "measuredAt": null
                         }
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("measuredAt"))
                .andExpect(jsonPath("$.errors[0].message").value("measuredAt is required"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testGetMeasurements() throws Exception
    {
        // Arrange
        TemperatureMeasurement first = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.now(),
            "sensor-001",
            null);

        TemperatureMeasurement second = new TemperatureMeasurement(
            new BigDecimal("72.7"),
            TemperatureUnit.FAHRENHEIT,
            OffsetDateTime.now(),
            "sensor-002",
            null);

        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")));

        List<TemperatureMeasurement> measurements = List.of(first, second);

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                measurements,
                expectedPageable,
                measurements.size());

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].temperature").value(21.5))
                .andExpect(jsonPath("$.content[0].unit").value("CELSIUS"))
                .andExpect(jsonPath("$.content[0].sensorId").value("sensor-001"))
                .andExpect(jsonPath("$.content[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.content[1].temperature").value(72.7))
                .andExpect(jsonPath("$.content[1].unit").value("FAHRENHEIT"))
                .andExpect(jsonPath("$.content[1].sensorId").value("sensor-002"))
                .andExpect(jsonPath("$.content[1].createdAt").doesNotExist());
        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testGetMeasurementsBySensorId() throws Exception
    {
        // Arrange
        TemperatureMeasurement first = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.now(),
            "sensor-001",
            null);

        TemperatureMeasurement second = new TemperatureMeasurement(
            new BigDecimal("72.7"),
            TemperatureUnit.FAHRENHEIT,
            OffsetDateTime.now(),
            "sensor-001",
            null);

        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")));

        List<TemperatureMeasurement> measurements = List.of(first, second);

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                measurements,
                expectedPageable,
                measurements.size());

        when(service.findBySensorId("sensor-001", expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements").param("sensorId", "sensor-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].temperature").value(21.5))
                .andExpect(jsonPath("$.content[0].unit").value("CELSIUS"))
                .andExpect(jsonPath("$.content[0].sensorId").value("sensor-001"))
                .andExpect(jsonPath("$.content[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.content[1].temperature").value(72.7))
                .andExpect(jsonPath("$.content[1].unit").value("FAHRENHEIT"))
                .andExpect(jsonPath("$.content[1].sensorId").value("sensor-001"))
                .andExpect(jsonPath("$.content[1].createdAt").doesNotExist());
        // Verify
        verify(service).findBySensorId("sensor-001", expectedPageable);
    }

    @Test
    void testGetMeasurementsBySensorIdAndTimeRange() throws Exception
    {
        // Arrange
        TemperatureMeasurement first = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.now(),
            "sensor-001",
            null);

        TemperatureMeasurement second = new TemperatureMeasurement(
            new BigDecimal("72.7"),
            TemperatureUnit.FAHRENHEIT,
            OffsetDateTime.now(),
            "sensor-001",
            null);

        List<TemperatureMeasurement> measurements = List.of(first, second);

        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                measurements,
                expectedPageable,
                measurements.size());

        when(service.findBySensorIdAndMeasuredAtBetween(
                "sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"),
                OffsetDateTime.parse("2026-09-07T12:00:00Z"),
                expectedPageable)).thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sensorId", "sensor-001")
                .param("from", "2026-09-07T10:00:00Z")
                .param("to", "2026-09-07T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].sensorId").value("sensor-001"))
                .andExpect(jsonPath("$.content[1].sensorId").value("sensor-001"));
        // Verify
        verify(service).findBySensorIdAndMeasuredAtBetween(
                "sensor-001",
                OffsetDateTime.parse("2026-09-07T10:00:00Z"),
                OffsetDateTime.parse("2026-09-07T12:00:00Z"),
                expectedPageable);
    }

    @Test
    void testRejectTimeRangeWithoutSensorId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("from", "2026-09-07T10:00:00Z")
                .param("to", "2026-09-07T12:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("SENSOR_ID_REQUIRED"))
                .andExpect(jsonPath("$.detail").value("sensorId is required when using a time range"));


        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectIncompleteTimeRangeWithoutTo() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sensorId", "sensor-001")
                .param("from", "2026-09-07T10:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INCOMPLETE_TIME_RANGE"))
                .andExpect(jsonPath("$.detail").value("from and to must be provided together"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectIncompleteTimeRangeWithoutFrom() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sensorId", "sensor-001")
                .param("to", "2026-09-07T10:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INCOMPLETE_TIME_RANGE"))
                .andExpect(jsonPath("$.detail").value("from and to must be provided together"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectInvalidTimeRange() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sensorId", "sensor-001")
                .param("from", "2026-09-07T12:00:00Z")
                .param("to", "2026-09-07T10:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_TIME_RANGE"))
                .andExpect(jsonPath("$.detail").value("from must not be after to"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testGetMeasurementsWithoutParams() throws Exception
    {
        // Arrange
        OffsetDateTime measuredAt = OffsetDateTime.parse("2026-09-22T10:00:00+02:00");
        TemperatureMeasurement first = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            measuredAt,
            "sensor-001",
            null);

        TemperatureMeasurement second = new TemperatureMeasurement(
            new BigDecimal("72.7"),
            TemperatureUnit.FAHRENHEIT,
            measuredAt,
            "sensor-002",
            null);

        List<TemperatureMeasurement> measurements = List.of(first, second);

        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                measurements,
                expectedPageable,
                measurements.size());

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].temperature").value(21.5))
                .andExpect(jsonPath("$.content[0].unit").value("CELSIUS"))
                .andExpect(jsonPath("$.content[0].sensorId").value("sensor-001"))
                .andExpect(jsonPath("$.content[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.content[1].temperature").value(72.7))
                .andExpect(jsonPath("$.content[1].unit").value("FAHRENHEIT"))
                .andExpect(jsonPath("$.content[1].sensorId").value("sensor-002"))
                .andExpect(jsonPath("$.content[1].createdAt").doesNotExist());

        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testGetMeasurementsWithPageAndSize() throws Exception
    {
        // Arrange
        TemperatureMeasurement measurement = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-09-22T10:00:00+02:00"),
            "sensor-001",
            null);

        Pageable expectedPageable = PageRequest.of(
                2,
                25,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                List.of(measurement),
                expectedPageable,
                51);

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("page", "2")
                .param("size", "25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(25))
                .andExpect(jsonPath("$.totalElements").value(51))
                .andExpect(jsonPath("$.totalPages").value(3));

        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testNegativePage() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter 'page'"));

        // Verify
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    void testRejectNonPositiveSize(int size) throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter 'size'"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectSizeAboveMaximum() throws Exception
    {
        // Arrange
        int size = MAX_PAGE_SIZE + 1;
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("PAGE_SIZE_TOO_LARGE"))
                .andExpect(jsonPath("$.detail").value("size must not be greater than " + MAX_PAGE_SIZE));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testAcceptMaximumSize() throws Exception
    {
        // Arrange
        TemperatureMeasurement measurement = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-09-22T10:00:00+02:00"),
            "sensor-001",
            null);

        Pageable expectedPageable = PageRequest.of(
                0,
                MAX_PAGE_SIZE,
                Sort.by(
                        Sort.Order.desc("measuredAt"),
                        Sort.Order.asc("sensorId")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                List.of(measurement),
                expectedPageable,
                1);

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("size", String.valueOf(MAX_PAGE_SIZE)))
                .andExpect(status().isOk());

        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testGetMeasurementsWithCustomSort() throws Exception
    {
        // Arrange
        TemperatureMeasurement measurement = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-09-22T10:00:00+02:00"),
            "sensor-001",
            null);

        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.asc("measuredAt"),
                        Sort.Order.asc("sensorId")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                List.of(measurement),
                expectedPageable,
                51);

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sort", "measuredAt,asc"))
                .andExpect(status().isOk());

        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testSortWithoutDirectionUsesAscendingDefault() throws Exception
    {
        // Arrange
        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.asc("measuredAt"),
                        Sort.Order.asc("sensorId")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(List.of(), expectedPageable, 0);

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sort", "measuredAt"))
                .andExpect(status().isOk());

        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testGetMeasurementsSortedBySensorId() throws Exception
    {
        // Arrange
        TemperatureMeasurement measurement = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-09-22T10:00:00+02:00"),
            "sensor-001",
            null);

        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.asc("sensorId"),
                        Sort.Order.desc("measuredAt")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                List.of(measurement),
                expectedPageable,
                51);

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sort", "sensorId,asc"))
                .andExpect(status().isOk());

        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testGetMeasurementsDoubleSorting() throws Exception
    {
        // Arrange
        TemperatureMeasurement measurement = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-09-22T10:00:00+02:00"),
            "sensor-001",
            null);

        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.asc("sensorId"),
                        Sort.Order.desc("measuredAt")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                List.of(measurement),
                expectedPageable,
                51);

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sort", "sensorId,asc")
                .param("sort", "measuredAt,desc"))
                .andExpect(status().isOk());

        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testGetMeasurementsWithMultipleSortDirections() throws Exception
    {
        // Arrange
        TemperatureMeasurement measurement = new TemperatureMeasurement(
            new BigDecimal("21.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.parse("2026-09-22T10:00:00+02:00"),
            "sensor-001",
            null);

        Pageable expectedPageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Order.asc("measuredAt"),
                        Sort.Order.desc("sensorId")));

        Page<TemperatureMeasurement> measurementsPage = new PageImpl<>(
                List.of(measurement),
                expectedPageable,
                51);

        when(service.findAll(expectedPageable))
                .thenReturn(measurementsPage);

        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sort", "measuredAt,asc")
                .param("sort", "sensorId,desc"))
                .andExpect(status().isOk());

        // Verify
        verify(service).findAll(expectedPageable);
    }

    @Test
    void testRejectSortByUnsupportedProperty() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("sort", "temperature,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_SORT_PROPERTY"))
                .andExpect(jsonPath("$.detail").value("Sorting by temperature is not allowed"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectGetMeasurementsWithInvalidFromParameter() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("from", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter 'from'"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testGetMeasurementsWithInvalidPageParameter() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter 'page'"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testGetMeasurementsWithInvalidSizeParameter() throws Exception
    {
        // Act + Assert
        mockMvc.perform(get("/api/measurements")
                .param("size", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter 'size'"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithoutTerminalId() throws Exception {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "temperature": 23.5,
                                "unit": "CELSIUS",
                                "sensorId": "sensor-001",
                                "measuredAt": "2026-10-20T08:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("terminalId"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("terminalId must not be blank"));

        // Verify
        verifyNoInteractions(service);
    }

    @Test
    void testRejectMeasurementWithBlankTerminalId() throws Exception {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "temperature": 23.5,
                                "unit": "CELSIUS",
                                "sensorId": "sensor-001",
                                "terminalId": "   ",
                                "measuredAt": "2026-10-20T08:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[?(@.field == 'terminalId')].message", hasItem("terminalId must not be blank")));

        // Verify
        verifyNoInteractions(service);
    }
        @Test
    void testRejectMeasurementWithTerminalIdTooLong() throws Exception
    {
        // Arrange
        String terminalId = "t".repeat(101);
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                        "temperature": 23.5,
                        "unit": "CELSIUS",
                        "sensorId": "sensor-001",
                        "terminalId": "%s",
                        "measuredAt": "2026-10-20T08:00:00Z"
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
    void testRejectMeasurementWithInvalidTerminalId() throws Exception
    {
        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "temperature": 23.5,
                        "unit": "CELSIUS",
                        "sensorId": "sensor-001",
                        "terminalId": "terminal A",
                        "measuredAt": "2026-10-20T08:00:00Z"
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
    void testRegisterMeasurementWithTerminalIdSpecialCharacters() throws Exception
    {
        // Arrange
        String terminalId = "terminal._-" + "t".repeat(89);

        TemperatureMeasurement saved = new TemperatureMeasurement(
            new BigDecimal("23.5"),
            TemperatureUnit.CELSIUS,
            OffsetDateTime.now(),
            "sensor-001",
            terminalId);

        when(service.save(any(TemperatureMeasurementRequest.class)))
            .thenReturn(new MeasurementIngestionResult(
                MeasurementIngestionStatus.STORED,
                saved,
                null
        ));

        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {
                         "temperature": 23.5,
                         "unit": "CELSIUS",
                         "sensorId": "sensor-001",
                         "terminalId": "%s",
                         "measuredAt": "2026-10-20T08:00:00Z"
                         }
                         """.formatted(terminalId)))
                .andExpect(status().isOk());

        // Verify
        ArgumentCaptor<TemperatureMeasurementRequest> captor = ArgumentCaptor.forClass(TemperatureMeasurementRequest.class);

        verify(service).save(captor.capture());

        TemperatureMeasurementRequest received = captor.getValue();
        // Assert
        assertEquals(terminalId, received.getTerminalId());
    }

    @Test
    void testQuarantineMeasurementWithUnknownSensor() throws Exception {
        // Arrange
        when(service.save(any(TemperatureMeasurementRequest.class)))
            .thenReturn(new MeasurementIngestionResult(
                MeasurementIngestionStatus.QUARANTINED,
                null,
                QuarantinedReason.UNKNOWN_SENSOR));

        // Act + Assert
        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "temperature": 23.5,
                    "unit": "CELSIUS",
                    "sensorId": "sensor_unknown",
                    "terminalId": "terminal-A",
                    "measuredAt": "2026-10-21T08:00:00Z"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(
                    MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("QUARANTINED"))
            .andExpect(jsonPath("$.reason").value("UNKNOWN_SENSOR"))
            .andExpect(jsonPath("$.temperature").doesNotExist());

        // Verify
        verify(service).save(any(TemperatureMeasurementRequest.class));
    }

}
