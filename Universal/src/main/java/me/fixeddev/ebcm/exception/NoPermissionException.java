package me.fixeddev.ebcm.exception;

public class NoPermissionException extends CommandUsageException {
    public NoPermissionException() {
        this("No permission.");
    }

    public NoPermissionException(String message) {
        super(message);
    }
}
