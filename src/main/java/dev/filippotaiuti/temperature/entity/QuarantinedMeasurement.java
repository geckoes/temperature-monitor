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

@Entity
@Table(name="quarantined_measurements")
public class QuarantinedMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 6, scale = 2, nullable = false)
    private BigDecimal temperature;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private TemperatureUnit unit;

    @Column(name = "sensor_id", length = 100, nullable = false)
    private String sensorId;

    @Column(name = "terminal_id", length = 100, nullable = false)
    private String terminalId;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private QuarantinedReason reason;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    protected QuarantinedMeasurement() {
        // Richiesto da JPA
    }

    public QuarantinedMeasurement(
            BigDecimal temperature,
            TemperatureUnit unit,
            String sensorId,
            String terminalId,
            OffsetDateTime measuredAt,
            QuarantinedReason reason,
            OffsetDateTime receivedAt) {
        this.temperature = temperature;
        this.unit = unit;
        this.sensorId = sensorId;
        this.terminalId = terminalId;
        this.measuredAt = measuredAt;
        this.reason = reason;
        this.receivedAt = receivedAt;
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
    public String getSensorId() {
        return sensorId;
    }
    public String getTerminalId() {
        return terminalId;
    }
    public OffsetDateTime getMeasuredAt() {
        return measuredAt;
    }
    public QuarantinedReason getReason() {
        return reason;
    }
    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }
}
