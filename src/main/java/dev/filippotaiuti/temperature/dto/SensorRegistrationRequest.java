package dev.filippotaiuti.temperature.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record SensorRegistrationRequest(
    @NotBlank(message = "sensorId must not be blank")
    @Size(max = 100, message = "sensorId must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]*", message = "sensorId contains invalid characters")
    String sensorId,
    @NotBlank(message = "terminalId must not be blank")
    @Size(max = 100, message = "terminalId must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]*", message = "terminalId contains invalid characters")
    String terminalId,
    @Size(max = 255, message = "name must not exceed 255 characters")
    String name
)
{
}
