package me.fixeddev.ebcm.internal.namespace;

import me.fixeddev.ebcm.NamespaceAccesor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Namespace implements NamespaceAccesor {
    private Map<Class<?>, Map<String, Object>> backing;

    public Namespace() {
        backing = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> clazz, String name) {
        return (T) backing.getOrDefault(clazz, new ConcurrentHashMap<>()).get(name);
    }

    public <T> void setObject(Class<T> clazz, String name, T object) {
        Map<String, Object> map = backing.getOrDefault(clazz, new ConcurrentHashMap<>());
        map.put(name, object);

        backing.put(clazz, map);
    }
}