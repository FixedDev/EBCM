package me.fixeddev.ebcm;

public interface NamespaceAccesor {
    @SuppressWarnings("unchecked")
    <T> T getObject(Class<T> clazz, String name);
}
