package me.fixeddev.ebcm.part;

public interface CommandPart {
    String getName();

    String getDescription();

    boolean isRequired();
}
