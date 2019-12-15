package me.fixeddev.ebcm;

import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class CommandData {
    public abstract String getName();

    public abstract List<String> getAliases();

    public abstract String getDescription();

    public static Builder builder(String name) {
        return new AutoValue_CommandData.Builder()
                .named(name)
                .setAliases(Collections.EMPTY_LIST)
                .setDescription("");
    }

    @AutoValue.Builder
    public abstract static class Builder {
        final Builder named(String name) {
            return setName(name);
        }

        abstract Builder setName(String name);

        public abstract Builder setAliases(List<String> aliases);

        public abstract Builder setDescription(String description);

        public abstract CommandData build();
    }
}
