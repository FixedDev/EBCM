package me.fixeddev.ebcm;

public interface Messager {
    default void sendMessage(NamespaceAccesor namespaceAccesor, String message) {
        sendMessage(namespaceAccesor, "UNKNOWN", message);
    }

    void sendMessage(NamespaceAccesor namespaceAccesor, String messageId, String message, String... parameters);
}
