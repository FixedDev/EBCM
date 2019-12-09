package me.fixeddev.ebcm.part;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.List;

@AutoValue
public abstract class SubCommandPart implements LineConsumingPart {

    public abstract List<Command> getCommandsToCall();

    public static Builder builder(String name) {
        return new AutoValue_SubCommandPart.Builder()
                .named(name)
                .required()
                .setDescription("");
    }

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

        final Builder named(String name) {
            return setName(name);
        }

        abstract Builder setName(String newName);

        final Builder required() {
            return setRequired(true);
        }

        abstract Builder setRequired(boolean newRequired);

        public abstract Builder setDescription(String newDescription);

        public Builder addCommandToCall(Command command){
            commandsToCallAppender.add(command);

            return this;
        }

        public Builder setCommands(List<Command> commands){
            commandsToCallAppender.set(commands);

            return this;
        }

        abstract Builder setCommandsToCall(List<Command> commands);

        public SubCommandPart build() {
            setCommandsToCall(commandsToCallAppender.toList());

            return autoBuild();
        }

        public abstract SubCommandPart autoBuild();
    }
}
