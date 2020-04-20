package me.fixeddev.ebcm;

import me.fixeddev.ebcm.part.CommandPart;

import java.util.List;
import java.util.Optional;

public interface CommandContext extends NamespaceAccesor {
    /**
     * @return The executed command, that means that if you execute a subcommand
     * this command will return that subcommand instead of the main command
     */
    Command getCommand();

    /**
     * @return The raw arguments passed to the command manager
     */
    List<String> getArguments();

    String getLabel();

    boolean has(CommandPart part);

    default List<CommandPart> getParts(String name) {
        return getCommand().getPartWithName(name);
    }

    Optional<List<String>> getRaw(CommandPart part);

    List<CommandPart> getBoundParts();

    <V> Optional<V> getValue(CommandPart part);

    /**
     * The difference between this and the {@link CommandContext#getValue(CommandPart)} method is that this method allows null values(being valid) to be returned
     * In certain cases it can be useful, since the provider can return a null, but still be a valid value
     *
     * @param part The part to retrieve the value from
     * @param <V> The returned object type
     * @return The nullable object associated as the value for the specified part
     * @throws IllegalArgumentException If the specified part doesn't has a value associated with
     */
    <V> V getRawValue(CommandPart part);

    boolean hasValue(CommandPart part);
}
