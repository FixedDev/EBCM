package me.fixeddev.ebcm.part;

import com.google.auto.value.AutoValue;
import me.fixeddev.ebcm.util.ListAppender;

import java.util.List;

@AutoValue
public abstract class InjectedValuePart implements CommandPart {
    public static Builder builder(String name, Class<?> type) {
        return new AutoValue_InjectedValuePart.Builder()
                .setName(name)
                .setInjectedName(name)
                .setType(type)
                .setRequired(true);
    }

    public abstract Class<?> getType();

    public abstract String getInjectedName();

    @AutoValue.Builder
    public abstract static class Builder {
        private ListAppender<String> modifiersAppender = new ListAppender<>();

        abstract Builder setName(String newName);

        public abstract Builder setDescription(String newDescription);

        public abstract Builder setRequired(boolean newRequired);

        public abstract Builder setType(Class<?> type);

        public abstract Builder setInjectedName(String newInjectedName);

        public Builder setAllModifiers(List<String> modifiers) {
            this.modifiersAppender.set(modifiers);

            return this;
        }

        public Builder addModifier(String modifier) {
            this.modifiersAppender.add(modifier);

            return this;
        }

        protected abstract Builder setModifiers(List<String> modifiers);

        protected abstract InjectedValuePart autoBuild();

        public InjectedValuePart build() {
            setModifiers(modifiersAppender.toList());

            return autoBuild();
        }
    }
}
