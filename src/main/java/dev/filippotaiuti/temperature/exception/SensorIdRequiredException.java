package dev.filippotaiuti.temperature.exception;

public class SensorIdRequiredException extends RuntimeException {
	
	public SensorIdRequiredException() {
		super("sensorId is required when using a time range");
	}
}
