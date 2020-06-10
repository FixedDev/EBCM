package me.fixeddev.ebcm.parametric;

public class CommandBuildException extends RuntimeException {

    private boolean stackTrace = false;

    public CommandBuildException(String message) {
        super(message);
    }

    public CommandBuildException(String message, Throwable cause) {
        super(message, cause);
        stackTrace = true;

        fillInStackTrace();
    }

    public CommandBuildException(Throwable cause) {
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
