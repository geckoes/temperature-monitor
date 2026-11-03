package dev.filippotaiuti.temperature.exception;

public class SensorNotFoundException extends RuntimeException {

    public SensorNotFoundException(String sensorId) {
        super("Sensor '" + sensorId + "' not found.");
    }
}

