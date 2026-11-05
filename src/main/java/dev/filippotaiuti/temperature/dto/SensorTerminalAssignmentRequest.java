package dev.filippotaiuti.temperature.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SensorTerminalAssignmentRequest(
    @NotBlank(message = "terminalId nust not be blank") @Size(max = 100, message = "terminalId must not exceed 100 characters") @Pattern(regexp = "^[a-zA-Z0-9._-]*", message = "terminalId contains invalid characters") String terminalId) {
}
