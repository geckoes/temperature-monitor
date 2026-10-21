package dev.filippotaiuti.temperature.dto;

import dev.filippotaiuti.temperature.entity.MeasurementIngestionStatus;
import dev.filippotaiuti.temperature.entity.QuarantinedReason;

public record QuarantinedMeasurementResponse(
    MeasurementIngestionStatus status, QuarantinedReason reason
) {

}
