package dev.filippotaiuti.temperature.dto;

import dev.filippotaiuti.temperature.entity.MeasurementIngestionStatus;
import dev.filippotaiuti.temperature.entity.QuarantinedReason;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;

public record MeasurementIngestionResult(MeasurementIngestionStatus status, TemperatureMeasurement measurement, QuarantinedReason reason) {

}
