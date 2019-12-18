package me.fixeddev.ebcm.part;

import java.util.List;

public interface CommandPart {
    String getName();

    String getDescription();

    boolean isRequired();

    /**
     * You may ask, what are the modifiers?
     * The modifiers are properties set at the moment of the part creation
     * Those are used on the parameter providers to do things like joining strings with
     * specific character instead of the generic space
     * @return The list of modifiers for this part
     */
    List<String> getModifiers();
}
