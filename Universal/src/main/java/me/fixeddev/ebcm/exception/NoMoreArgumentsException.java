package me.fixeddev.ebcm.exception;

public class NoMoreArgumentsException extends CommandParseException {
    public NoMoreArgumentsException(int size, int position) {
        super("No more arguments were found, size: " + size + " position: " + position);
    }

    public NoMoreArgumentsException() {
    }

    public NoMoreArgumentsException(String message) {
        super(message);
    }

    public NoMoreArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMoreArgumentsException(Throwable cause) {
        super(cause);
    }
}
