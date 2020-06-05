package me.fixeddev.ebcm.i18n;

import me.fixeddev.ebcm.NamespaceAccesor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultI18n implements I18n {
    private final Map<String, String> messageMap;

    public DefaultI18n() {
        messageMap = new ConcurrentHashMap<>();

        messageMap.put(Message.COMMAND_NO_PERMISSIONS.getId(), null);
        messageMap.put(Message.COMMAND_USAGE.getId(), null);
        messageMap.put(Message.INVALID_SUBCOMMAND.getId(), "Invalid sub-command, valid values: %s");
        messageMap.put(Message.MISSING_ARGUMENT.getId(), "Missing arguments for required part %s minimum arguments required: %s");
        messageMap.put(Message.MISSING_SUBCOMMAND.getId(), "Missing argument for required part %s, available values: %s");
    }

    @Override
    public String getMessage(String messageId, NamespaceAccesor namespace) {
        String message = messageMap.get(messageId);

        if (message != null) {
            return message;
        }

        Message messageObject = Message.findMessage(messageId);

        if (messageObject == null) {
            return null;
        }

        messageId = messageObject.getId();

        return messageMap.get(messageId);
    }
}
