package dev.filippotaiuti.temperature.mapper;

import org.springframework.stereotype.Component;

import dev.filippotaiuti.temperature.dto.SensorResponse;
import dev.filippotaiuti.temperature.entity.Sensor;

@Component
public class SensorResponseMapper {

    public SensorResponse toResponse(Sensor sensor) {
        return new SensorResponse(
            sensor.getSensorId(),
            sensor.getTerminalId(),
            sensor.getName(),
            sensor.getCreatedAt()
        );
    }
}
