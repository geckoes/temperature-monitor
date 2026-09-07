package dev.filippotaiuti.temperature.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;

public interface TemperatureMeasurementRepository
        extends JpaRepository<TemperatureMeasurement, Long>
{
    /**
     * @param string
     * @return
     */
    List<TemperatureMeasurement> findBySensorId(String sensorId);

    /**
     * @param string
     * @param offsetDateTimeFrom
     * @param offsetDateTimeTo
     * @return
     */
    List<TemperatureMeasurement> findBySensorIdAndMeasuredAtBetween(String sensorId, OffsetDateTime offsetDateTimeFrom,
            OffsetDateTime offsetDateTimeTo);
}