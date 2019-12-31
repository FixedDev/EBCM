package me.fixeddev.ebcm.part;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.List;

@AutoValue
public abstract class SubCommandPart implements LineConsumingPart {

    public static Builder builder(String name) {
        return new AutoValue_SubCommandPart.Builder()
                .named(name)
                .setRequired(false)
                .setDescription("");
    }

    public abstract List<Command> getCommandsToCall();

    @Override
    @Memoized
    public String getLineRepresentation() {
        StringBuilder builder = new StringBuilder(getName().length() + 2);

        builder.append(isRequired() ? "<" : "[")
                .append(getName())
                .append(isRequired() ? ">" : "]");

        return builder.toString();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        private ListAppender<Command> commandsToCallAppender = new ListAppender<>();
        private ListAppender<String> modifiersAppender = new ListAppender<>();

        final Builder named(String name) {
            return setName(name);
        }

        abstract Builder setName(String newName);

        public abstract Builder setRequired(boolean newRequired);

        public abstract Builder setDescription(String newDescription);

        public Builder addCommand(Command command) {
            commandsToCallAppender.add(command);

            return this;
        }

        public Builder setCommands(List<Command> commands) {
            commandsToCallAppender.set(commands);

            return this;
        }

        abstract Builder setCommandsToCall(List<Command> commands);

        public Builder setAllModifiers(List<String> modifiers) {
            this.modifiersAppender.set(modifiers);

            return this;
        }

        public Builder addModifier(String modifier) {
            this.modifiersAppender.add(modifier);

            return this;
        }

        abstract Builder setModifiers(List<String> modifiers);

        public SubCommandPart build() {
            setCommandsToCall(commandsToCallAppender.toList());
            setModifiers(modifiersAppender.toList());

            return autoBuild();
        }

        public abstract SubCommandPart autoBuild();
    }
}
