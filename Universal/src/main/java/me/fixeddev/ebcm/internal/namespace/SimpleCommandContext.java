package me.fixeddev.ebcm.internal.namespace;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandContext;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.ParseResult;
import me.fixeddev.ebcm.part.CommandPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleCommandContext extends NamespaceAccessorDelegate implements CommandContext {
    private final Command executedCommand;
    private final List<String> rawArguments;
    private final String label;

    private final Map<String, List<CommandPart>> allParts;
    private final Map<CommandPart, List<String>> rawBindings;
    private final Map<CommandPart, Object> valueBindings;

    private List<CommandPart> boundParts;

    public SimpleCommandContext(NamespaceAccesor namespace, ParseResult result) {
        super(namespace);

        allParts = new HashMap<>();

        for (CommandPart part : result.getCommandToExecute().getParts()) {
            allParts.computeIfAbsent(part.getName(), k -> new ArrayList<>()).add(part);
        }

        for (CommandPart part : result.getMainCommand().getParts()) {
            allParts.computeIfAbsent(part.getName(), k -> new ArrayList<>()).add(part);
        }

        rawBindings = new HashMap<>();

        for (ParseResult.ParameterBinding binding : result.getBindings()) {
            rawBindings.put(binding.getBind(), binding.getRaw());
        }

        valueBindings = result.getValueBindings();

        label = result.getLabel();
        rawArguments = result.getCommandLine();
        executedCommand = result.getCommandToExecute();
    }

    @Override
    public Command getCommand() {
        return executedCommand;
    }

    @Override
    public List<String> getArguments() {
        return rawArguments;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean has(CommandPart part) {
        return rawBindings.containsKey(part);
    }

    @Override
    public List<CommandPart> getParts(String name) {
        return allParts.computeIfAbsent(name, k -> new ArrayList<>());
    }

    @Override
    public Optional<List<String>> getRaw(CommandPart part) {
        return Optional.ofNullable(rawBindings.get(part));
    }

    @Override
    public List<CommandPart> getBoundParts() {
        if (boundParts == null) {
            boundParts = new ArrayList<>(valueBindings.keySet());
        }

        return boundParts;
    }

    @Override
    public <V> Optional<V> getValue(CommandPart part) {
        return (Optional<V>) Optional.ofNullable(valueBindings.get(part));
    }

    @Override
    public <V> V getRawValue(CommandPart part) {
        if (!hasValue(part)) {
            throw new IllegalArgumentException("The part " + part.getName() + " doesn't has a value associated with!");
        }

        return (V) valueBindings.get(part);
    }

    @Override
    public boolean hasValue(CommandPart part) {
        return valueBindings.containsKey(part);
    }
}
