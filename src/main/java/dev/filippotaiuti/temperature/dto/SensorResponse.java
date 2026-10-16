package dev.filippotaiuti.temperature.dto;

import java.time.OffsetDateTime;

public record SensorResponse(
    String sensorId,
    String terminalId,
    String name,
    OffsetDateTime createdAt
) {

}
