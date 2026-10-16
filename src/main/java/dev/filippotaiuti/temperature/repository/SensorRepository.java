package dev.filippotaiuti.temperature.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.filippotaiuti.temperature.entity.Sensor;

public interface SensorRepository extends JpaRepository<Sensor, String> {

    boolean existsByTerminalId(String terminalId);

}
