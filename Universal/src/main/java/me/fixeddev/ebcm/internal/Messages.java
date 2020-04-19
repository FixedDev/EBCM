package me.fixeddev.ebcm.internal;

import java.util.StringJoiner;

public enum Messages {
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

    private String id;

    Messages(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getId(String... commandName) {
        return id;
    }

    @Override
    public String toString() {
        return getId();
    }
}
