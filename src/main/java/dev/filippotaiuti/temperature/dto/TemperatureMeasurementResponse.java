package dev.filippotaiuti.temperature.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;

public class TemperatureMeasurementResponse
{
    private Long id;
    private BigDecimal temperature;
    private TemperatureUnit unit;
    private String sensorId;
    private String terminalId;
    private OffsetDateTime measuredAt;

    public TemperatureMeasurementResponse(
            Long id,
            BigDecimal temperature,
            TemperatureUnit unit,
            String sensorId,
            String terminalId,
            OffsetDateTime measuredAt)
    {

        this.id = id;
        this.temperature = temperature;
        this.unit = unit;
        this.sensorId = sensorId;
        this.terminalId = terminalId;
        this.measuredAt = measuredAt;
    }

    public Long getId()
    {
        return id;
    }

    public BigDecimal getTemperature()
    {
        return temperature;
    }

    public TemperatureUnit getUnit()
    {
        return unit;
    }

    public String getSensorId()
    {
        return sensorId;
    }

    public String getTerminalId()
    {
        return terminalId;
    }

    public OffsetDateTime getMeasuredAt()
    {
        return measuredAt;
    }

    public static TemperatureMeasurementResponse from(
            TemperatureMeasurement measurement)
    {

        return new TemperatureMeasurementResponse(
                measurement.getId(),
                measurement.getTemperature(),
                measurement.getUnit(),
                measurement.getSensorId(),
                measurement.getTerminalId(),
                measurement.getMeasuredAt());
    }

}
