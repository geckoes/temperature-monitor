package dev.filippotaiuti.temperature.exception;

public class UnsupportedSortPropertyException extends RuntimeException {

	public UnsupportedSortPropertyException(String property) {
		super("Sorting by " + property + " is not allowed");
	}
}
