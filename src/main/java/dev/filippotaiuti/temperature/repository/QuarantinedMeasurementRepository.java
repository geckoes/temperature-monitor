package dev.filippotaiuti.temperature.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.filippotaiuti.temperature.entity.QuarantinedMeasurement;

public interface QuarantinedMeasurementRepository extends JpaRepository<QuarantinedMeasurement, Long> {

    public QuarantinedMeasurement save(QuarantinedMeasurement measurement);
}
