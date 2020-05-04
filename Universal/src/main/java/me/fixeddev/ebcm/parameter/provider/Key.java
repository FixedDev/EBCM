package me.fixeddev.ebcm.parameter.provider;

import java.util.Objects;

public class Key<T> {
    private String modifier;
    private Class<T> clazz;

    public Key(String modifier, Class<T> clazz) {
        this.modifier = modifier;
        this.clazz = clazz;
    }

    public Key(Class<T> clazz) {
        this.clazz = clazz;
    }

    public String getModifier() {
        return modifier;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key)) return false;
        Key<?> key = (Key<?>) o;
        return Objects.equals(modifier, key.modifier) &&
                Objects.equals(clazz, key.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifier, clazz);
    }

    @Override
    public String toString() {
        return "Key{" +
                "'" + modifier + '\'' +
                ": " + clazz +
                '}';
    }
}
