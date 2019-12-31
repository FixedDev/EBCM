package me.fixeddev.ebcm;

import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class CommandData {
    public static Builder builder(String name) {
        return new AutoValue_CommandData.Builder()
                .named(name)
                .setAliases(Collections.emptyList())
                .setDescription("");
    }

    public abstract String getName();

    public abstract List<String> getAliases();

    public abstract String getDescription();

    @AutoValue.Builder
    public abstract static class Builder {
        protected final Builder named(String name) {
            return setName(name);
        }

        protected abstract Builder setName(String name);

        public abstract Builder setAliases(List<String> aliases);

        public abstract Builder setDescription(String description);

        public abstract CommandData build();
    }
}
