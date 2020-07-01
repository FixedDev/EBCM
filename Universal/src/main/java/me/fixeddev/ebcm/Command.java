package me.fixeddev.ebcm;

import me.fixeddev.ebcm.part.CommandPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Command {
    CommandData getData();

    String getPermission();

    String getPermissionMessage();

    String getUsage();

    CommandAction getAction();

    List<CommandPart> getParts();

    Optional<CommandUsageHandler> getUsageHandler();

    default List<CommandPart> getPartWithName(String name) {
        List<CommandPart> matchingParts = new ArrayList<>();

        for (CommandPart part : getParts()) {
            if (part.getName().equals(name)) {
                matchingParts.add(part);
            }
        }

        return matchingParts;
    }
}
