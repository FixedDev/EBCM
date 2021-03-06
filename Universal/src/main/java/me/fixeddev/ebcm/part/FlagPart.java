package me.fixeddev.ebcm.part;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.List;

@AutoValue
public abstract class FlagPart implements LineConsumingPart {

    public static Builder builder(String named, char flagChar) {
        return new AutoValue_FlagPart.Builder().named(named).flagChar(flagChar)
                .setDescription("");
    }

    @Override
    @Memoized
    public String getLineRepresentation() {
        return "[-" +
                getFlagChar() +
                "]";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    public abstract char getFlagChar();

    @AutoValue.Builder
    public abstract static class Builder {
        private ListAppender<String> modifiersAppender = new ListAppender<>();

        protected final Builder named(String name) {
            return setName(name);
        }

        protected final Builder flagChar(char flagChar) {
            return setFlagChar(flagChar);
        }

        protected abstract Builder setName(String newName);

        protected abstract Builder setFlagChar(char newFlagChar);

        public abstract Builder setDescription(String newDescription);

        public Builder setAllModifiers(List<String> modifiers) {
            this.modifiersAppender.set(modifiers);

            return this;
        }

        public Builder addModifier(String modifier) {
            this.modifiersAppender.add(modifier);

            return this;
        }

        protected abstract Builder setModifiers(List<String> modifiers);

        protected abstract FlagPart autoBuild();

        public final FlagPart build() {
            setModifiers(modifiersAppender.toList());

            return autoBuild();
        }
    }
}
