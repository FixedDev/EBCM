package me.fixeddev.ebcm.part;

/**
 * Represents a CommandPart that consumes an argument/part of the command
 * line
 */
public interface LineConsumingPart extends CommandPart {
    String getLineRepresentation();
}
