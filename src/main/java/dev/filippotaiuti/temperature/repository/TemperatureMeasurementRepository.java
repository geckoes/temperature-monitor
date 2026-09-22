package dev.filippotaiuti.temperature.repository;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;

public interface TemperatureMeasurementRepository
        extends JpaRepository<TemperatureMeasurement, Long>
{
    /**
     * @param string
     * @param pageable
     * @return
     */
    Page<TemperatureMeasurement> findBySensorId(String sensorId, Pageable pageable);

    /**
     * @param string
     * @param offsetDateTimeFrom
     * @param offsetDateTimeTo
     * @param pageable
     * @return
     */
    Page<TemperatureMeasurement> findBySensorIdAndMeasuredAtBetween(String sensorId, OffsetDateTime offsetDateTimeFrom,
            OffsetDateTime offsetDateTimeTo, Pageable pageable);
}