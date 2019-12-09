package me.fixeddev.ebcm;

import com.google.auto.value.AutoValue;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class Command {
    public abstract CommandData getData();

    public abstract String getPermission();

    public abstract String getPermissionMessage();

    public abstract CommandAction getAction();

    public abstract List<CommandPart> getParts();

    public List<CommandPart> getPartWithName(String name){
        List<CommandPart> matchingParts = new ArrayList<>();

        for (CommandPart part : getParts()) {
            if(part.getName().equals(name)){
                matchingParts.add(part);
            }
        }

        return matchingParts;
    }

    public static Builder builder(CommandData.Builder dataBuilder) {
        return new AutoValue_Command.Builder()
                .withData(dataBuilder)
                .setPermission("")
                .setPermissionMessage("No permission.")
                .setAction(params -> true);
    }

    @AutoValue.Builder
    public abstract static class Builder {

        private ListAppender<CommandPart> partListAppender = new ListAppender<>();

        final Builder withData(CommandData.Builder dataBuilder) {
            return setData(dataBuilder.build());
        }

        abstract Builder setData(CommandData newData);

        public abstract Builder setPermission(String permission);

        public abstract Builder setPermissionMessage(String message);

        public abstract Builder setAction(CommandAction newAction);

        abstract Builder setParts(List<CommandPart> newParts);

        public abstract Command autoBuild();

        public Builder setCommandParts(List<CommandPart> newParts){
            partListAppender.set(newParts);

            return this;
        }

        public Builder addPart(CommandPart part) {
            if(part == null) {
                throw new IllegalArgumentException("The provided part is null");
            }

            partListAppender.add(part);

            return this;
        }

        public Command build() {
            setParts(partListAppender.toList());

            return autoBuild();
        }

    }
}
