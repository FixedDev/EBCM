package me.fixeddev.ebcm;

import me.fixeddev.ebcm.part.CommandPart;

import java.util.List;
import java.util.Optional;

public interface CommandContext extends NamespaceAccesor {
    /**
     * @return The executed command, that means that if you execute a subcommand
     *          this command will return that subcommand instead of the main command
     */
    Command getCommand();

    /**
     * @return The raw arguments passed to the command manager
     */
    List<String> getArguments();

    String getLabel();

    boolean has(CommandPart part);

    default List<CommandPart> getParts(String name){
        return getCommand().getPartWithName(name);
    }

    Optional<List<String>> getRaw(CommandPart part);

    List<CommandPart> getBoundParts();

    <V> Optional<V> getValue(CommandPart part);
}
