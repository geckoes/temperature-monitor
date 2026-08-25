package dev.filippotaiuti.temperature.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import dev.filippotaiuti.temperature.entity.TemperatureUnit;

public class TemperatureMeasurementRequest {
    @NotNull
    private BigDecimal temperature;
    @NotNull
    private TemperatureUnit unit;
    @NotBlank
    private String sensorId;

    public TemperatureMeasurementRequest() {
    }

    public TemperatureMeasurementRequest(BigDecimal temperature, TemperatureUnit unit, String sensorId) {
        this.temperature = temperature;
        this.unit = unit;
        this.sensorId = sensorId;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public TemperatureUnit getUnit() {
        return unit;
    }

    public void setUnit(TemperatureUnit unit) {
        this.unit = unit;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
}