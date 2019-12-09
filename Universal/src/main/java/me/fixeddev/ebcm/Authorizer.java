package me.fixeddev.ebcm;

public interface Authorizer {
    boolean isAuthorized(NamespaceAccesor namespace, String permission);
}
