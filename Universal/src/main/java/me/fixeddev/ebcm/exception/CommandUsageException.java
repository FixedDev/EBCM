package me.fixeddev.ebcm.exception;

public class CommandUsageException extends CommandParseException {
    public CommandUsageException() {
        super();
    }

    public CommandUsageException(String message) {
        super(message);
    }

    public CommandUsageException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandUsageException(Throwable cause) {
        super(cause);
    }
}
