package me.fixeddev.ebcm.internal;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.ParseResult;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.CommandUsageException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.parameter.provider.Key;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.part.ArgumentPart;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.part.FlagPart;
import me.fixeddev.ebcm.part.InjectedValuePart;
import me.fixeddev.ebcm.part.SubCommandPart;
import me.fixeddev.ebcm.stack.ArgumentStack;
import me.fixeddev.ebcm.stack.BasicStackSlice;
import me.fixeddev.ebcm.stack.NoOpStackSlice;
import me.fixeddev.ebcm.stack.SimpleArgumentStack;
import me.fixeddev.ebcm.stack.StackSlice;
import me.fixeddev.ebcm.stack.StackSnapshot;
import me.fixeddev.ebcm.util.UsageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandLineParser {

    private List<Command> commandExecutionPath;
    private String commandLabel;

    // Per command fields start
    private List<CommandPart> currentCommandParts;
    private Command currentCommand;

    private int partsLeft;
    private ListIterator<CommandPart> partsIterator;
    private CommandPart currentPart;
    // Per command fields end

    private ArgumentStack argumentStack;

    private List<ParseResult.ParameterBinding> bindings;
    private Map<CommandPart, Object> valueBindings;

    private NamespaceAccesor namespaceAccesor;

    private CommandManager commandManager;

    private ParameterProviderRegistry providerRegistry;

    public CommandLineParser(List<String> argumentsLine, NamespaceAccesor namespaceAccesor, CommandManager commandManager) {
        commandLabel = "";

        argumentStack = new SimpleArgumentStack(argumentsLine);

        bindings = new ArrayList<>();
        valueBindings = new LinkedHashMap<>();

        this.commandManager = commandManager;
        this.namespaceAccesor = namespaceAccesor;
        providerRegistry = commandManager.getProviderRegistry();
        commandExecutionPath = new ArrayList<>();
    }

    public String nextArgument() throws CommandParseException {
        return argumentStack.next();
    }

    public boolean hasNextArgument() {
        return argumentStack.hasNext();
    }

    public CommandPart nextUnboundPart() throws CommandParseException {
        if (!partsIterator.hasNext()) {
            throw new CommandParseException("There're is not a next part!");
        }

        return (currentPart = partsIterator.next());
    }

    public boolean hasNextUnboundPart() {
        return partsIterator.hasNext();
    }

    public void bindPart(CommandPart part, List<String> arguments) {
        ParseResult.ParameterBinding binding = new ParameterBindingData(arguments, part);

        bindings.add(binding);
    }

    public Command currentAsRootCommand() throws CommandNotFound, CommandParseException {
        String currentArgument = argumentStack.current();

        if (!commandExecutionPath.isEmpty()) {
            throw new CommandParseException("The command was already found, no need to execute this method again!");
        }

        currentCommand = commandManager.getCommand(currentArgument)
                .orElseThrow(() -> new CommandNotFound("The current argument(" + currentArgument + ") can't be found as command!"));

        useCommand(currentCommand);

        return currentCommand;
    }

    public void useCommand(Command command) {
        currentCommand = command;

        commandExecutionPath.add(command);

        commandLabel += " " + argumentStack.current();
        currentCommandParts = new ArrayList<>(command.getParts());
        partsIterator = currentCommandParts.listIterator();
        partsLeft = currentCommandParts.size();
    }

    private void parseFlags() throws NoMoreArgumentsException {
        Map<Character, FlagPart> flagParts = currentCommandParts.stream()
                .filter(part -> part instanceof FlagPart)
                .map(part -> (FlagPart) part).collect(Collectors.toMap(FlagPart::getFlagChar, Function.identity()));

        currentCommandParts.removeAll(flagParts.values());
        partsIterator = currentCommandParts.listIterator();
        partsLeft = currentCommandParts.size();

        StackSnapshot snapshot = argumentStack.getSnapshot();

        boolean removedArg = false;
        while (argumentStack.hasNext()) {
            String argument;

            if (!removedArg) {
                argument = argumentStack.next();
            } else {
                argument = argumentStack.current();
                removedArg = false;
            }

            if (!argument.startsWith("-") || argument.length() != 2) {
                continue;
            }

            // Disable the parsing of the next flags
            if ("--".equals(argument)) {
                break;
            }

            char flagChar = argument.charAt(1);
            FlagPart part = flagParts.get(flagChar);

            // The flag is valid, but it doesn't has a part to be bound
            if (part == null) {
                continue;
            }

            bindings.add(new ParameterBindingData(argument, part));
            valueBindings.put(part, true);

            // We remove the part, at the end create a value binding for every flag that wasn't
            // in the command line with value false
            flagParts.remove(flagChar);
            argumentStack.remove();
            removedArg = true;
        }

        argumentStack.applySnapshot(snapshot, false);
        flagParts.values().forEach(part -> valueBindings.put(part, false));
    }

    private boolean hasSubCommand;
    private int neededArguments;
    private int allNeededArguments;
    // Ok, the name of this is not clear
    // But, this variable it's supposed to mean the quantity of optional parts
    // that can be bound before only the required parts can be bound
    private int optionalArgumentsToBound;

    public ParseResult parse() throws CommandParseException, CommandNotFound {
        nextArgument();
        currentAsRootCommand();
        parseFlags();

        hasSubCommand = currentCommandParts.subList(partsIterator.nextIndex(), currentCommandParts.size()).stream().anyMatch(part -> part instanceof SubCommandPart && part.isRequired());
        neededArguments = calculateNeededArgs();
        allNeededArguments = calculateAllNeededArgs();
        optionalArgumentsToBound = argumentStack.getArgumentsLeft() - neededArguments;

        if (partsLeft <= 0) {
            return new ParseResultData(commandLabel, argumentStack.getBacking(), commandExecutionPath, bindings, valueBindings);
        }

        checkForInvalidInfiniteParts();

        while (hasNextUnboundPart()) {
            CommandPart partToBind = nextUnboundPart();

            if (partToBind instanceof SubCommandPart) {
                parseSubCommand(partToBind);
            } else if (partToBind instanceof ArgumentPart) {
                parseArgument(partToBind);
            } else if (partToBind instanceof InjectedValuePart) {
                parseInjectedPart(partToBind);
            } else {
                throw new CommandParseException("Invalid part type provided! Type: " + partToBind.getClass().getSimpleName());
            }

        }

        return new ParseResultData(commandLabel.trim(), argumentStack.getBacking(), commandExecutionPath, bindings, valueBindings);
    }

    private void parseInjectedPart(CommandPart partToBind) throws CommandParseException {
        InjectedValuePart part = (InjectedValuePart) partToBind;

        String modifier = part.getModifiers().isEmpty() ? null : part.getModifiers().get(0);
        ParameterProvider<?> provider = providerRegistry.getParameterProvider(new Key<>(modifier, part.getType()));

        if (provider == null) {
            provider = providerRegistry.getParameterProvider(part.getType());
        }

        if (provider == null || !provider.isInjected()) {
            throw new CommandParseException("Failed to get a provider for the part " + part.getName());
        }

        ParameterProvider.Result<?> result = provider.transform(new NoOpStackSlice(), namespaceAccesor, part);

        Optional<?> optionalObject = unwrapObject(result, part);

        if (!optionalObject.isPresent() && part.isRequired()) {
            throw new CommandParseException("Failed to get the injected value for the part with name " + part.getName() +
                    "\n injected name: " + part.getInjectedName() +
                    "\n type: " + part.getType());
        }


        valueBindings.put(partToBind, optionalObject.orElse(null));
    }

    private void parseArgument(CommandPart partToBind) throws CommandParseException {
        ArgumentPart part = (ArgumentPart) partToBind;

        String modifier = part.getModifiers().isEmpty() ? null : part.getModifiers().get(0);
        ParameterProvider<?> provider = providerRegistry.getParameterProvider(new Key<>(modifier, part.getArgumentType()));

        if (provider == null) {
            provider = providerRegistry.getParameterProvider(part.getArgumentType());
        }

        if (provider == null || provider.isInjected()) {
            throw new CommandParseException("Failed to get a provider for the part " + part.getName());
        }

        StackSlice argumentsToUse = null;
        boolean usingDefaults = false;

        if (!part.isRequired()) {
            if ((optionalArgumentsToBound <= 0 || argumentStack.getArgumentsLeft() < neededArguments + part.getConsumedArguments()) ||
                    (allNeededArguments == -1 || part.getConsumedArguments() == -1 || hasSubCommand)) {
                if (part.getDefaultValues().isEmpty()) {
                    return;
                }
                List<String> defaults = part.getDefaultValues();

                usingDefaults = true;
                argumentsToUse = new BasicStackSlice(0, defaults.size(), -1, new SimpleArgumentStack(defaults));
                allNeededArguments = allNeededArguments - part.getConsumedArguments();
            }
        }

        int oldArgumentsLeft = argumentStack.getArgumentsLeft();

        if (!usingDefaults) {
            if (part.getConsumedArguments() == -1) {
                argumentsToUse = argumentStack.getSliceFrom(argumentStack.getPosition());
            } else {
                argumentsToUse = argumentStack.getSliceTo(argumentStack.getPosition() + part.getConsumedArguments());
            }
        }

        StackSnapshot start = argumentStack.getSnapshot();

        ParameterProvider.Result<?> object = provider.transform(argumentsToUse, namespaceAccesor, part);

        int usedArguments = oldArgumentsLeft - argumentStack.getArgumentsLeft();

        decrementArguments(part, usedArguments);
        bindPart(part, getUsedArguments(start, usedArguments));

        valueBindings.put(part, unwrapObject(object, part).orElse(null));
    }

    private List<String> getUsedArguments(StackSnapshot snapshot, int usedArguments) throws NoMoreArgumentsException {
        List<String> arguments = new ArrayList<>();

        while (snapshot.hasNext() && usedArguments > 0) {
            arguments.add(snapshot.next());
            usedArguments--;
        }

        return arguments;
    }

    private void decrementArguments(CommandPart part, int usedArguments) {
        if (part.isRequired()) {
            neededArguments -= usedArguments;
        } else {
            optionalArgumentsToBound -= usedArguments;
        }
        allNeededArguments -= usedArguments;
    }

    private Optional<?> unwrapObject(ParameterProvider.Result<?> result, CommandPart part) throws CommandParseException {
        Optional<?> providedObject = result.getResultObject();
        Optional<String> message = result.getMessage();
        Optional<Exception> lastError = result.lastError();

        if (!providedObject.isPresent()) {
            if (lastError.isPresent()) {
                throw new CommandParseException("An exception occurred while parsing the part " + part.getName() + " argument!", lastError.get());
            }

            message.ifPresent(s -> commandManager.getMessager().sendMessage(namespaceAccesor, s));

            return Optional.empty();
        }

        return providedObject;
    }

    private void parseSubCommand(CommandPart partToBind) throws CommandParseException {
        if (hasNextUnboundPart()) {
            throw new CommandParseException("The sub-command should be the last part of the command!");
        }

        SubCommandPart subCommandPart = (SubCommandPart) partToBind;
        Map<String, Command> availableValues = new HashMap<>();

        for (Command command : subCommandPart.getCommandsToCall()) {
            availableValues.put(command.getData().getName(), command);

            for (String value : command.getData().getAliases()) {
                availableValues.put(value.toLowerCase(), command);
            }
        }

        String availableValuesString = String.join(", ", availableValues.keySet());

        if (!hasNextArgument()) {
            if (partToBind.isRequired()) {
                commandManager.getMessager().sendMessage(namespaceAccesor, Messages.MISSING_SUBCOMMAND.getId(),
                        "Missing argument for required part %s, available values: %s", partToBind.getName(), availableValuesString);

                throw new CommandUsageException(UsageBuilder.getUsageForCommand(null, currentCommand, commandLabel));
            }

            return;
        }

        String argument = argumentStack.next();

        Command command = availableValues.get(argument.toLowerCase());

        if (command == null) {
            commandManager.getMessager().sendMessage(namespaceAccesor, Messages.INVALID_SUBCOMMAND.getId(),
                    "Invalid sub-command, valid values: %s", availableValuesString);


            throw new CommandUsageException(UsageBuilder.getUsageForCommand(null, currentCommand, commandLabel));
        }

        useCommand(command);
        parseFlags();

        hasSubCommand = currentCommandParts.subList(partsIterator.nextIndex(), currentCommandParts.size()).stream().anyMatch(part -> part instanceof SubCommandPart);
        neededArguments = calculateNeededArgs();
        allNeededArguments = calculateAllNeededArgs();

        ParseResult.ParameterBinding parameterBinding = new ParameterBindingData(argumentStack.current(), currentPart);
        bindings.add(parameterBinding);
    }


    private void checkForInvalidInfiniteParts() throws CommandParseException {
        boolean infinitePartFound = false;

        for (CommandPart part : currentCommandParts) {
            if (!(part instanceof ArgumentPart)) {
                continue;
            }

            ArgumentPart argumentPart = (ArgumentPart) part;

            if (argumentPart.getConsumedArguments() == -1) {
                infinitePartFound = true;
                continue;
            }

            if (argumentPart.isRequired() && infinitePartFound) {
                throw new CommandParseException("A required part was found after an infinite part!");
            }
        }
    }

    private int calculateNeededArgs() {
        int sum = 0;

        for (CommandPart part : currentCommandParts) {
            if (!(part instanceof ArgumentPart) || !part.isRequired()) {
                continue;
            }

            int consumedArgs = ((ArgumentPart) part).getConsumedArguments();

            if (consumedArgs == -1) {
                consumedArgs = 1;
            }

            sum += consumedArgs;
        }

        return sum;
    }

    private int calculateAllNeededArgs() {
        int sum = 0;

        for (CommandPart part : currentCommandParts) {
            if (!(part instanceof ArgumentPart)) {
                continue;
            }

            int consumedArgs = ((ArgumentPart) part).getConsumedArguments();

            if (consumedArgs == -1) {
                return -1;
            }

            sum += consumedArgs;
        }

        return sum;
    }

    static class ParameterBindingData implements ParseResult.ParameterBinding {

        private List<String> raw;
        private CommandPart boundPart;

        public ParameterBindingData(String raw, CommandPart boundPart) {
            this.raw = new ArrayList<>(1);
            this.raw.add(raw);

            this.boundPart = boundPart;
        }

        public ParameterBindingData(List<String> raw, CommandPart boundPart) {
            this.raw = raw;
            this.boundPart = boundPart;
        }

        @Override
        public List<String> getRaw() {
            return raw;
        }

        @Override
        public CommandPart getBind() {
            return boundPart;
        }
    }

    static class ParseResultData implements ParseResult {

        private String label;
        private List<String> commandLine;
        private List<Command> executionPath;
        private List<ParameterBinding> parameterBindings;
        private Map<CommandPart, Object> valueBindings;

        public ParseResultData(String label,
                               List<String> commandLine,
                               List<Command> executionPath,
                               List<ParameterBinding> parameterBindings,
                               Map<CommandPart, Object> valueBindings) {
            this.label = label;
            this.commandLine = commandLine;
            this.executionPath = executionPath;
            this.parameterBindings = parameterBindings;
            this.valueBindings = valueBindings;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public List<String> getCommandLine() {
            return commandLine;
        }

        @Override
        public List<Command> getCommandExecutionPath() {
            return executionPath;
        }

        @Override
        public Command getMainCommand() {
            return executionPath.get(0);
        }

        @Override
        public Command getCommandToExecute() {
            return executionPath.get(executionPath.size() - 1);
        }

        public List<ParameterBinding> getBindings() {
            return parameterBindings;
        }

        @Override
        public Map<CommandPart, Object> getValueBindings() {
            return valueBindings;
        }
    }
}
