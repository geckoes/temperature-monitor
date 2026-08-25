package dev.filippotaiuti.temperature.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name="temperature_measurements",
    uniqueConstraints = @UniqueConstraint(
        name = "temperature_measurements_sensor_measured_at_unique",
        columnNames = {"sensor_id", "measured_at"}
    )
)

public class TemperatureMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 6, scale = 2, nullable = false)
    private BigDecimal temperature;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private TemperatureUnit unit;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "sensor_id", nullable = false)
    private String sensorId;

    protected TemperatureMeasurement() {
        // Default constructor for JPA
    }

    public TemperatureMeasurement(BigDecimal temperature, TemperatureUnit unit, OffsetDateTime measuredAt, String sensorId) {
        this.temperature = temperature;
        this.unit = unit;
        this.measuredAt = measuredAt;
        this.sensorId = sensorId;
        this.createdAt = OffsetDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }
    public BigDecimal getTemperature() {
        return temperature;
    }
    public TemperatureUnit getUnit() {
        return unit;
    }
    public OffsetDateTime getMeasuredAt() {
        return measuredAt;  
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public String getSensorId() {
        return sensorId;
    }
}