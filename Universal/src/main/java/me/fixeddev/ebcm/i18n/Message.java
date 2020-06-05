package me.fixeddev.ebcm.i18n;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public enum Message {

    COMMAND_USAGE("command.usage") {
        @Override
        public String getId(String... commandName) {
            StringJoiner joiner = new StringJoiner(".");
            joiner.add(getId());

            for (String s : commandName) {
                joiner.add(s);
            }

            return joiner.toString();
        }
    },
    COMMAND_NO_PERMISSIONS("command.no.permissions") {
        @Override
        public String getId(String... commandName) {
            StringJoiner joiner = new StringJoiner(".");
            joiner.add(getId());

            for (String s : commandName) {
                joiner.add(s);
            }

            return joiner.toString();
        }
    },
    MISSING_ARGUMENT("missing.argument"),
    MISSING_SUBCOMMAND("missing.subcommand"),
    INVALID_SUBCOMMAND("invalid.subcommand");

    private static final Map<String, Message> messages;

    static {
        messages = new ConcurrentHashMap<>();

        for (Message value : values()) {
            value.getId();
        }
    }

    private String id;

    Message(String id) {
        this.id = id;
    }

    public String getId() {
        messages.put(id, this);

        return id;
    }

    public String getId(String... commandName) {
        return id;
    }

    @Override
    public String toString() {
        return getId();
    }

    public static Message findMessage(String id) {
        Message firstFound = messages.get(id);

        if (firstFound != null) {
            return firstFound;
        }

        StringBuilder messagePath = new StringBuilder();

        boolean first = true;
        for (String part : id.split(".")) {
            if (first) {
                messagePath.append(part);

                first = false;
            } else {
                messagePath.append(".").append(part);
            }

            Message message = messages.get(messagePath.toString());

            if (message != null) {
                messages.put(id, message);

                return message;
            }
        }

        return null;
    }
}
