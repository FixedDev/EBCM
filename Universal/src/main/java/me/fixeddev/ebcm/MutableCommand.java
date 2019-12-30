package me.fixeddev.ebcm;

import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.List;

public class MutableCommand implements Command {

    private final CommandData data;
    private String permission;
    private String permissionMessage;
    private CommandAction action;
    private List<CommandPart> parts;

    private MutableCommand(CommandData data, String permission, String permissionMessage, CommandAction action, List<CommandPart> parts) {
        this.data = data;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.action = action;
        this.parts = parts;
    }

    @Override
    public CommandData getData() {
        return data;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getPermissionMessage() {
        return permissionMessage;
    }

    @Override
    public CommandAction getAction() {
        return action;
    }

    @Override
    public List<CommandPart> getParts() {
        return parts;
    }

    public static Builder builder(CommandData.Builder dataBuilder) {
        return new MutableCommand.Builder(dataBuilder.build())
                .setPermission("")
                .setPermissionMessage("No permission.")
                .setAction(params -> false);
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
    }

    public void setAction(CommandAction action) {
        this.action = action;
    }

    public void setParts(List<CommandPart> parts) {
        this.parts = parts;
    }

    public static class Builder {
        private CommandData data;
        private String permission;
        private String permissionMessage;
        private CommandAction action;

        private ListAppender<CommandPart> partListAppender = new ListAppender<>();

        Builder(CommandData data) {
            if (data == null) {
                throw new NullPointerException("Null data");
            }

            this.data = data;
        }

        public Builder setPermission(String permission) {
            if (permission == null) {
                throw new NullPointerException("Null permission");
            }

            this.permission = permission;
            return this;
        }

        public Builder setPermissionMessage(String permissionMessage) {
            if (permissionMessage == null) {
                throw new NullPointerException("Null permissionMessage");
            }

            this.permissionMessage = permissionMessage;
            return this;
        }

        public Builder setAction(CommandAction action) {
            if (action == null) {
                throw new NullPointerException("Null action");
            }

            this.action = action;
            return this;
        }

        public Builder setCommandParts(List<CommandPart> newParts) {
            partListAppender.set(newParts);

            return this;
        }

        public Builder addPart(CommandPart part) {
            if (part == null) {
                throw new IllegalArgumentException("The provided part is null");
            }

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
                    this.permission,
                    this.permissionMessage,
                    this.action,
                    this.partListAppender.toList());
        }
    }
}
