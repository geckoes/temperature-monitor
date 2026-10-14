package dev.filippotaiuti.temperature.exception;

public class IncompleteTimeRangeException extends RuntimeException {
	
	public IncompleteTimeRangeException() {
		super("from and to must be provided together");
	}
}
