package me.fixeddev.ebcm.part;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;

@AutoValue
public abstract class FlagPart implements LineConsumingPart {

    @Override
    @Memoized
    public String getLineRepresentation() {
        StringBuilder builder = new StringBuilder(3);

        builder.append("[-")
                .append(getFlagChar())
                .append("]");

        return builder.toString();
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    public abstract char getFlagChar();

    public static Builder builder(String named, char flagChar) {
        return new AutoValue_FlagPart.Builder().named(named).flagChar(flagChar)
                .setDescription("");
    }

    @AutoValue.Builder
    public abstract static class Builder {
        final Builder named(String name) {
            return setName(name);
        }

        final Builder flagChar(char flagChar) {
            return setFlagChar(flagChar);
        }

        abstract Builder setName(String newName);

        abstract Builder setFlagChar(char newFlagChar);

        public abstract Builder setDescription(String newDescription);

        public abstract FlagPart build();
    }
}
