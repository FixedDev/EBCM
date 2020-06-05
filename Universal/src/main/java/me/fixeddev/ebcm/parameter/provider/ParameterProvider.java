package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.SuggestionProvider;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.stack.StackSlice;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface ParameterProvider<T> extends SuggestionProvider {

    Result<T> transform(StackSlice slice, NamespaceAccesor namespaceAccesor, CommandPart part) throws NoMoreArgumentsException;

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

        static <T> Result<T> createResultOfMessage(String message, String... messageParameters) {
            return new SimpleResult<>(message, Arrays.asList(messageParameters));
        }

        static <T> Result<T> createResult(String message, List<Exception> errors, String... messageParameters) {
            return new SimpleResult<>(message, Arrays.asList(messageParameters), errors);
        }

        static <T> Result<T> createResult(String message, String[] messageParameters, Exception... errors) {
            return new SimpleResult<>(message, Arrays.asList(messageParameters), Arrays.asList(errors));
        }

        static <T> Result<T> createResult(T object, String message, List<Exception> errors, String... messageParameters) {
            return new SimpleResult<>(object, message, Arrays.asList(messageParameters), errors);
        }

        Optional<T> getResultObject();

        Optional<String> getRawMessage();

        /**
         * Formats the raw message if the message is not an i18n translate path
         * @return An optional formatted message or i18n path
         */
        Optional<String> getMessage();

        boolean isI18nPath();

        List<Object> getMessageParameters();

        List<Exception> getErrors();

        Optional<Exception> lastError();
    }
}
