package dev.filippotaiuti.temperature.exception;

public class TerminalAlreadyAssignedException extends RuntimeException {

    public TerminalAlreadyAssignedException(String terminalId) {
        super("Terminal '" + terminalId + "'' is already assigned.");
    }

}
