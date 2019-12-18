package me.fixeddev.ebcm.part;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.ArrayList;
import java.util.List;

@AutoValue
public abstract class ArgumentPart implements ArgumentConsumingPart {

    public abstract int getConsumedArguments();

    @Override
    @Memoized
    public String getLineRepresentation() {
        StringBuilder builder = new StringBuilder(getName().length() + 2);

        builder.append(isRequired() ? "<" : "[")
                .append(getName())
                .append(isRequired() ? ">" : "]");

        return builder.toString();
    }

    public static Builder builder(String name, Class<?> argumentType) {
        return new AutoValue_ArgumentPart.Builder()
                .named(name)
                .argumentType(argumentType)
                .setConsumedArguments(1)
                .setRequired(false)
                .setDescription("")
                .setDefaultValues(new ArrayList<>());
    }

    @AutoValue.Builder
    public abstract static class Builder {
        private ListAppender<String> modifiersAppender = new ListAppender<>();

        final Builder named(String name) {
            return setName(name);
        }

        abstract Builder setName(String newName);

        final Builder argumentType(Class<?> argumentType) {
            return setArgumentType(argumentType);
        }

        abstract Builder setArgumentType(Class<?> newArgumentType);

        public abstract Builder setDefaultValues(List<String> defaultValues);

        public abstract Builder setConsumedArguments(int consumedArguments);

        public abstract Builder setDescription(String newDescription);

        public abstract Builder setRequired(boolean newRequired);

        public Builder setAllModifiers(List<String> modifiers){
            this.modifiersAppender.set(modifiers);

            return this;
        }

        public Builder addModifier(String modifier){
            this.modifiersAppender.add(modifier);

            return this;
        }

        abstract Builder setModifiers(List<String> modifiers);

        abstract boolean isRequired();

        abstract int getConsumedArguments();

        abstract ArgumentPart autoBuild();

        public ArgumentPart build() {
            if (isRequired() && getConsumedArguments() == -1) {
                throw new IllegalArgumentException("A part can't be required and infinite!");
            }

            setModifiers(modifiersAppender.toList());

            return autoBuild();
        }
    }
}
