package me.fixeddev.ebcm.parameter.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class SimpleResult<T> implements ParameterProvider.Result<T> {
    private T object;
    private String message;
    private List<Exception> errors;

    private Exception lastError;

    SimpleResult(T object) {
        this.object = object;
        errors = Collections.emptyList();
    }

    SimpleResult(String message) {
        this.message = message;
        errors = Collections.emptyList();
    }

    SimpleResult(String message, List<Exception> errors) {
        this.message = message;
        this.errors = errors;
    }

    SimpleResult(T object, String message, List<Exception> errors) {
        this.object = object;
        this.message = message;
        this.errors = errors;
    }

    @Override
    public Optional<T> getResultObject() {
        return Optional.ofNullable(object);
    }

    @Override
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    @Override
    public List<Exception> getErrors() {
        return errors;
    }

    @Override
    public Optional<Exception> lastError() {
        if (lastError == null && !errors.isEmpty()) {
            lastError = errors.get(errors.size() - 1);
        }

        return Optional.ofNullable(lastError);
    }
}
