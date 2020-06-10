package me.fixeddev.ebcm.exception;

public class CommandParseException extends Exception {
    private boolean stackTrace = false;

    public CommandParseException() {
        super();
    }

    public CommandParseException(String message) {
        super(message);
    }

    public CommandParseException(String message, Throwable cause) {
        super(message, cause);
        stackTrace = true;

        fillInStackTrace();
    }

    public CommandParseException(Throwable cause) {
        super(cause);
        stackTrace = true;

        fillInStackTrace();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (!stackTrace) {
            return this;
        }

        return super.fillInStackTrace();
    }
}
