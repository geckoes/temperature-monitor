package dev.filippotaiuti.temperature.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import dev.filippotaiuti.temperature.dto.PagedResponse;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementResponse;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;

class TemperatureMeasurementResponseMapperTest
{
    @Test
    void testToPagedResponse()
    {
        // Arrange
        TemperatureMeasurementResponseMapper mapper =
                new TemperatureMeasurementResponseMapper();

        OffsetDateTime measuredAt =
                OffsetDateTime.parse("2026-09-22T10:00:00+02:00");

        TemperatureMeasurement first = new TemperatureMeasurement(
                new BigDecimal("21.5"),
                TemperatureUnit.CELSIUS,
                measuredAt,
                "sensor-001");

        TemperatureMeasurement second = new TemperatureMeasurement(
                new BigDecimal("72.7"),
                TemperatureUnit.FAHRENHEIT,
                measuredAt,
                "sensor-002");

        Pageable pageable = PageRequest.of(1, 2);

        Page<TemperatureMeasurement> page =
                new PageImpl<>(
                        List.of(first, second),
                        pageable,
                        5
                );

        // Act
        PagedResponse<TemperatureMeasurementResponse> result =
                mapper.toPagedResponse(page);

        // Assert - pagination metadata
        assertEquals(1, result.page());
        assertEquals(2, result.size());
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages());

        // Assert - content
        assertEquals(2, result.content().size());

        assertEquals(
                "sensor-001",
                result.content().get(0).getSensorId());

        assertEquals(
                "sensor-002",
                result.content().get(1).getSensorId());
    }
}
