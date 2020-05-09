package me.fixeddev.ebcm.part;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import me.fixeddev.ebcm.SuggestionProvider;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AutoValue
public abstract class ArgumentPart implements ArgumentConsumingPart {

    public static Builder builder(String name, Class<?> argumentType) {
        return new AutoValue_ArgumentPart.Builder()
                .named(name)
                .argumentType(argumentType)
                .setConsumedArguments(1)
                .setRequired(false)
                .setDescription("")
                .setDefaultValues(new ArrayList<>());
    }

    public abstract Optional<SuggestionProvider> getSuggestionProvider();

    public abstract int getConsumedArguments();

    @Override
    @Memoized
    public String getLineRepresentation() {

        return (isRequired() ? "<" : "[") +
                getName() +
                (isRequired() ? ">" : "]");
    }

    @AutoValue.Builder
    public abstract static class Builder {
        private ListAppender<String> modifiersAppender = new ListAppender<>();

        protected final Builder named(String name) {
            return setName(name);
        }

        protected abstract Builder setName(String newName);

        protected final Builder argumentType(Class<?> argumentType) {
            return setArgumentType(argumentType);
        }

        protected abstract Builder setArgumentType(Class<?> newArgumentType);

        public abstract Builder setDefaultValues(List<String> defaultValues);

        public abstract Builder setDescription(String newDescription);

        public abstract Builder setSuggestionProvider(SuggestionProvider suggestionProvider);

        public Builder setAllModifiers(List<String> modifiers) {
            this.modifiersAppender.set(modifiers);

            return this;
        }

        public Builder addModifier(String modifier) {
            this.modifiersAppender.add(modifier);

            return this;
        }

        protected abstract Builder setModifiers(List<String> modifiers);

        protected abstract boolean isRequired();

        public abstract Builder setRequired(boolean newRequired);

        protected abstract int getConsumedArguments();

        public abstract Builder setConsumedArguments(int consumedArguments);

        protected abstract ArgumentPart autoBuild();

        public ArgumentPart build() {
            setModifiers(modifiersAppender.toList());

            return autoBuild();
        }
    }
}
