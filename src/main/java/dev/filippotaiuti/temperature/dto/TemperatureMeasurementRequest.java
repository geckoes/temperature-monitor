package dev.filippotaiuti.temperature.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TemperatureMeasurementRequest
{
    @NotNull
    private BigDecimal temperature;
    @NotNull
    private TemperatureUnit unit;
    @NotBlank
    private String sensorId;
    @NotNull
    private OffsetDateTime measuredAt;

    public TemperatureMeasurementRequest()
    {
    }

    public TemperatureMeasurementRequest(BigDecimal temperature, TemperatureUnit unit, String sensorId,
            OffsetDateTime measuredAt)
    {
        this.temperature = temperature;
        this.unit = unit;
        this.sensorId = sensorId;
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

    public OffsetDateTime getMeasuredAt()
    {
        return measuredAt;
    }

    public void setMeasuredAt(OffsetDateTime measuredAt)
    {
        this.measuredAt = measuredAt;
    }
}