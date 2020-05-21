package me.fixeddev.ebcm;

import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.List;
import java.util.Objects;

public class MutableCommand implements Command {

    private final CommandData data;
    private String usage;
    private String permission;
    private String permissionMessage;
    private CommandAction action;
    private List<CommandPart> parts;

    private MutableCommand(CommandData data, String usage, String permission, String permissionMessage, CommandAction action, List<CommandPart> parts) {
        this.data = data;
        this.usage = usage;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.action = action;
        this.parts = parts;
    }

    public static Builder builder(CommandData.Builder dataBuilder) {
        return new MutableCommand.Builder(dataBuilder.build())
                .setPermission("")
                .setPermissionMessage("No permission.")
                .setAction(params -> false);
    }

    @Override
    public CommandData getData() {
        return data;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public String getPermissionMessage() {
        return permissionMessage;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    public void setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
    }

    @Override
    public CommandAction getAction() {
        return action;
    }

    public void setAction(CommandAction action) {
        this.action = action;
    }

    @Override
    public List<CommandPart> getParts() {
        return parts;
    }

    public void setParts(List<CommandPart> parts) {
        this.parts = parts;
    }

    public static class Builder {
        private CommandData data;
        private String usage;
        private String permission;
        private String permissionMessage;
        private CommandAction action;

        private ListAppender<CommandPart> partListAppender = new ListAppender<>();

        protected Builder(CommandData data) {
            Objects.requireNonNull(data);

            this.data = data;
        }

        public Builder setPermission(String permission) {
            Objects.requireNonNull(permission);

            this.permission = permission;
            return this;
        }

        public Builder setPermissionMessage(String permissionMessage) {
            Objects.requireNonNull(permissionMessage);

            this.permissionMessage = permissionMessage;
            return this;
        }

        public Builder setUsage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder setAction(CommandAction action) {
            Objects.requireNonNull(action);

            this.action = action;
            return this;
        }

        public Builder setCommandParts(List<CommandPart> newParts) {
            partListAppender.set(newParts);

            return this;
        }

        public Builder addPart(CommandPart part) {
            Objects.requireNonNull(part);

            partListAppender.add(part);

            return this;
        }

        public MutableCommand build() {
            String missing = "";
            if (this.data == null) {
                missing += " data";
            }
            if (this.permission == null) {
                missing += " permission";
            }
            if (this.permissionMessage == null) {
                missing += " permissionMessage";
            }
            if (this.action == null) {
                missing += " action";
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            return new MutableCommand(
                    this.data,
                    this.usage,
                    this.permission,
                    this.permissionMessage,
                    this.action,
                    this.partListAppender.toList());
        }
    }
}
