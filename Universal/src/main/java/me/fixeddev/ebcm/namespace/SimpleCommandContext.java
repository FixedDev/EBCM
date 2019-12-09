package me.fixeddev.ebcm.namespace;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandContext;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.ParseResult;
import me.fixeddev.ebcm.part.CommandPart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleCommandContext extends NamespaceAccessorDelegate implements CommandContext {
    private Command executedCommand;
    private List<String> rawArguments;
    private String label;

    private Map<CommandPart, List<String>> rawBindings;
    private Map<CommandPart, Object> valueBindings;

    public SimpleCommandContext(NamespaceAccesor namespace, ParseResult result) {
        super(namespace);

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
    public Optional<List<String>> getRaw(CommandPart part) {
        return Optional.ofNullable(rawBindings.get(part));
    }

    @Override
    public <V> Optional<V> getValue(CommandPart part) {
        return (Optional<V>) Optional.ofNullable(valueBindings.get(part));
    }
}
