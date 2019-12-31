package me.fixeddev.ebcm.exception;

public class CommandNotFound extends Exception {
    public CommandNotFound() {
        super();
    }

    public CommandNotFound(String message) {
        super(message);
    }

    public CommandNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandNotFound(Throwable cause) {
        super(cause);
    }
}
