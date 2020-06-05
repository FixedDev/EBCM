package me.fixeddev.ebcm.parameter.provider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class SimpleResult<T> implements ParameterProvider.Result<T> {
    private T object;
    private String message;
    private List<Exception> errors;
    private List<Object> parameters;

    private boolean i18n;

    private Exception lastError;

    SimpleResult(T object) {
        this.object = object;
        errors = Collections.emptyList();
        parameters = Collections.emptyList();
    }

    SimpleResult(String message, List<Object> parameters) {
        this.message = message;
        errors = Collections.emptyList();
        this.parameters = parameters == null ? Collections.emptyList() : parameters;
    }

    SimpleResult(String message, List<Object> parameters, List<Exception> errors) {
        this.message = message;
        this.errors = errors;
        this.parameters = parameters == null ? Collections.emptyList() : parameters;
    }

    SimpleResult(T object, String message, List<Object> parameters, List<Exception> errors) {
        this.object = object;
        this.message = message;
        this.errors = errors;
        this.parameters = parameters == null ? Collections.emptyList() : parameters;
    }

    private void setMessage(String message){
        if(!message.startsWith("%") || !message.endsWith("%")){
            this.message = message;
            i18n = false;

            return;
        }

        this.message = message;
        this.i18n = true;
    }

    @Override
    public Optional<T> getResultObject() {
        return Optional.ofNullable(object);
    }

    @Override
    public Optional<String> getRawMessage() {
        return Optional.ofNullable(message);
    }

    @Override
    public Optional<String> getMessage() {
        return getRawMessage().map(s -> !i18n ? String.format(s, getMessageParameters()) : message);
    }

    @Override
    public boolean isI18nPath() {
        return i18n;
    }

    @Override
    public List<Object> getMessageParameters() {
        return parameters;
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
