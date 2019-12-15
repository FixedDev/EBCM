package me.fixeddev.ebcm;

import com.google.auto.value.AutoValue;
import jdk.nashorn.internal.ir.annotations.Immutable;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.List;

@AutoValue
public abstract class ImmutableCommand implements Command {

    public static Builder builder(CommandData.Builder dataBuilder) {
        return new AutoValue_ImmutableCommand.Builder()
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

        public abstract ImmutableCommand autoBuild();

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

        public ImmutableCommand build() {
            setParts(partListAppender.toList());

            return autoBuild();
        }

    }
}