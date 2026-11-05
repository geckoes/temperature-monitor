package dev.filippotaiuti.temperature.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class TemperatureMeasurementRequest
{
    @NotNull(message = "temperature is required")
    private BigDecimal temperature;
    @NotNull(message = "unit is required")
    private TemperatureUnit unit;
    @NotBlank(message = "sensorId must not be blank")
    private String sensorId;
    @NotBlank(message = "terminalId must not be blank")
    @Size(max = 100, message = "terminalId must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]*", message = "terminalId contains invalid characters")
    private String terminalId;
    @NotNull(message = "measuredAt is required")
    private OffsetDateTime measuredAt;

    public TemperatureMeasurementRequest()
    {
    }

    public TemperatureMeasurementRequest(BigDecimal temperature, TemperatureUnit unit, String sensorId,
            String terminalId, OffsetDateTime measuredAt)
    {
        this.temperature = temperature;
        this.unit = unit;
        this.sensorId = sensorId;
        this.terminalId = terminalId;
        this.measuredAt = measuredAt;
    }

    public BigDecimal getTemperature()
    {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature)
    {
        this.temperature = temperature;
    }

    public TemperatureUnit getUnit()
    {
        return unit;
    }

    public void setUnit(TemperatureUnit unit)
    {
        this.unit = unit;
    }

    public String getSensorId()
    {
        return sensorId;
    }

    public void setSensorId(String sensorId)
    {
        this.sensorId = sensorId;
    }

    public String getTerminalId()
    {
        return terminalId;
    }

    public void setTerminalId(String terminalId)
    {
        this.terminalId = terminalId;
    }

    public OffsetDateTime getMeasuredAt()
    {
        return measuredAt;
    }

    public void setMeasuredAt(OffsetDateTime measuredAt)
    {
        this.measuredAt = measuredAt;
    }
}