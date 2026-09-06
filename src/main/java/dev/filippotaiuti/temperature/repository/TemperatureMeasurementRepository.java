package dev.filippotaiuti.temperature.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;

public interface TemperatureMeasurementRepository
        extends JpaRepository<TemperatureMeasurement, Long>
{
    List<TemperatureMeasurement> findBySensorId(String string);
}