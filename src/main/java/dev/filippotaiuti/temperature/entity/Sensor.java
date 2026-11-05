package dev.filippotaiuti.temperature.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "sensors")
public class Sensor {

    @Id
    @Column (name="sensor_id", length = 100, nullable = false)
    private String sensorId;

    @Column (name="terminal_id", length = 100, unique = true)
    private String terminalId;
    @Column (length = 100)
    private String name;

    @Column (name="created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Sensor() {
        // Default constructor for JPA
    }

    public Sensor(String sensorId, String terminalId, String name, OffsetDateTime createdAt) {
        this.sensorId = sensorId;
        this.terminalId = terminalId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
