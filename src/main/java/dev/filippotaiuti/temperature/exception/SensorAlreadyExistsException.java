package dev.filippotaiuti.temperature.exception;

public class SensorAlreadyExistsException extends RuntimeException {

    public SensorAlreadyExistsException(String sensorId) {
        super("Sensor '" + sensorId + "' already exists.");
    }
}
