package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.part.CommandPart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface ParameterProvider<T> {

    Result<T> transform(List<String> arguments, NamespaceAccesor namespaceAccesor, CommandPart part);

    default List<String> getSuggestions(String startsWith) {
        return Collections.emptyList();
    }

    default boolean isInjected() {
        return false;
    }

    interface Result<T> {
        static <T> Result<T> createResult(T object) {
            return new SimpleResult<>(object);
        }

        static <T> Result<T> createResultOfMessage(String message) {
            return new SimpleResult<>(message);
        }

        static <T> Result<T> createResult(String message, List<Exception> errors) {
            return new SimpleResult<>(message, errors);
        }

        static <T> Result<T> createResult(String message, Exception... errors) {
            return new SimpleResult<>(message, Arrays.asList(errors));
        }

        static <T> Result<T> createResult(T object, String message, List<Exception> errors) {
            return new SimpleResult<>(object, message, errors);
        }

        Optional<T> getResultObject();

        Optional<String> getMessage();

        List<Exception> getErrors();

        Optional<Exception> lastError();
    }
}
