package me.fixeddev.ebcm;

public interface Messenger {
    void sendMessage(NamespaceAccesor namespaceAccesor, String message, Object... parameters);
}
