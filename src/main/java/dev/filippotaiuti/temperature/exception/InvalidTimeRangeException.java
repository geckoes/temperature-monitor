package dev.filippotaiuti.temperature.exception;

public class InvalidTimeRangeException extends RuntimeException {
	
	public InvalidTimeRangeException() {
		super("from must not be after to");
	}
}
