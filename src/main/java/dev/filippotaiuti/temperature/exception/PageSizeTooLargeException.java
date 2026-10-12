package dev.filippotaiuti.temperature.exception;

public class PageSizeTooLargeException extends RuntimeException {

	public PageSizeTooLargeException(int maxSize) {
		super("size must not be greater than " + maxSize);
	}
}
