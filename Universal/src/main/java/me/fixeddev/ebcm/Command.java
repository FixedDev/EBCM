package me.fixeddev.ebcm;

import me.fixeddev.ebcm.part.CommandPart;

import java.util.ArrayList;
import java.util.List;

public interface Command {
    CommandData getData();

    String getPermission();

    String getPermissionMessage();

    CommandAction getAction();

    List<CommandPart> getParts();

    default List<CommandPart> getPartWithName(String name){
        List<CommandPart> matchingParts = new ArrayList<>();

        for (CommandPart part : getParts()) {
            if(part.getName().equals(name)){
                matchingParts.add(part);
            }
        }

        return matchingParts;
    }
}
