package me.fixeddev.ebcm;

import com.google.auto.value.AutoValue;
import me.fixeddev.ebcm.part.CommandPart;

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
                .setParts(Collections.emptyList())
                .setPermission("")
                .setPermissionMessage("No permission.");
    }

    @AutoValue.Builder
    public abstract static class Builder {

        final Builder withData(CommandData.Builder dataBuilder) {
            return setData(dataBuilder.build());
        }

        abstract Builder setData(CommandData newData);

        public abstract Builder setPermission(String permission);

        public abstract Builder setPermissionMessage(String message);

        public abstract Builder setAction(CommandAction newAction);

        public abstract Builder setParts(List<CommandPart> newParts);

        public abstract Command build();
    }
}
