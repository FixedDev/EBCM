package me.fixeddev.ebcm.exception;

public class CommandParseException extends Exception {
    private boolean stackTrace = true;

    public CommandParseException() {
        super();
    }

    public CommandParseException(String message) {
        super(message);
        stackTrace = false;
    }

    public CommandParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandParseException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if(!stackTrace){
            return this;
        }

        return super.fillInStackTrace();
    }
}
