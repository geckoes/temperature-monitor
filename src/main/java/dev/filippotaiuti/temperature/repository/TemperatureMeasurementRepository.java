package dev.filippotaiuti.temperature.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;

public interface TemperatureMeasurementRepository 
    extends JpaRepository<TemperatureMeasurement, Long> {
}