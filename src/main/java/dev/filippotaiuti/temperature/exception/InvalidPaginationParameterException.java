package dev.filippotaiuti.temperature.exception;

public class InvalidPaginationParameterException extends IllegalArgumentException {
    public InvalidPaginationParameterException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidPaginationParameterException(String message) {
        this(message,null);
    }
}
