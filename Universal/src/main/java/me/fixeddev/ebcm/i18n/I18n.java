package me.fixeddev.ebcm.i18n;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.NamespaceAccesor;

import java.util.List;

public interface I18n {
    String getMessage(String messageId, NamespaceAccesor namespace);

    default String getMessage(Message message, List<Command> executionPath, NamespaceAccesor namespace) {
        String[] commandPath = executionPath
                .stream()
                .map(cmd -> cmd.getData().getName()).toArray(String[]::new);

        return getMessage(message.getId(commandPath), namespace);
    }
}
