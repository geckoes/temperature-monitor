package dev.filippotaiuti.temperature.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import dev.filippotaiuti.temperature.dto.SensorRegistrationRequest;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.exception.SensorAlreadyExistsException;
import dev.filippotaiuti.temperature.exception.TerminalAlreadyAssignedException;
import dev.filippotaiuti.temperature.repository.SensorRepository;

@Service
public class SensorService {

    private final SensorRepository repository;

    public SensorService(SensorRepository repository) {
        this.repository = repository;
    }

    public Sensor register(SensorRegistrationRequest request) {
        if (repository.existsById(request.sensorId())) {
            throw new SensorAlreadyExistsException(request.sensorId());
        }
        if (repository.existsByTerminalId(request.terminalId())) {
            throw new TerminalAlreadyAssignedException(request.terminalId());
        }
        Sensor sensor = new Sensor(
            request.sensorId(),
            request.terminalId(),
            request.name(),
            OffsetDateTime.now()
        );
        return repository.save(sensor);
    }
}
