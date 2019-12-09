package me.fixeddev.ebcm.namespace;

import me.fixeddev.ebcm.NamespaceAccesor;


public class NamespaceAccessorDelegate implements NamespaceAccesor {
    private NamespaceAccesor delegate;

    public NamespaceAccessorDelegate(NamespaceAccesor delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> T getObject(Class<T> clazz, String name) {
        return delegate.getObject(clazz, name);
    }
}
