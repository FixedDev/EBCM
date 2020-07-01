package me.fixeddev.ebcm.internal;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.ParseResult;
import me.fixeddev.ebcm.ParsingContext;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.CommandUsageException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.i18n.Message;
import me.fixeddev.ebcm.parameter.provider.Key;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.part.ArgumentPart;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.part.CommandPartParser;
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

    private boolean hasSubCommand;
    private int neededArguments;
    private int allNeededArguments;
    // Ok, the name of this is not clear
    // But, this variable it's supposed to mean the quantity of optional parts
    // that can be bound before only the required parts can be bound
    private int optionalArgumentsToBound;

    private boolean stopParse;

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

    public Command nextAsCommand() throws CommandNotFound, CommandParseException {
        String currentArgument = argumentStack.next();

        return commandManager.getCommand(currentArgument)
                .orElseThrow(() -> new CommandNotFound("The current argument(" + currentArgument + ") can't be found as command!"));
    }

    public void useCommand(Command command) {
        currentCommand = command;
        commandExecutionPath.add(command);

        if (!commandManager.getAuthorizer().isAuthorized(namespaceAccesor, command.getPermission())) {
            String message = commandManager.getI18n().getMessage(Message.COMMAND_NO_PERMISSIONS, commandExecutionPath, namespaceAccesor);

            if (message == null) {
                message = command.getPermissionMessage();
            }

            commandManager.getMessenger().sendMessage(namespaceAccesor, message);

            stopParse();
            return;
        }

        currentCommand = command;

        commandExecutionPath.add(command);

        commandLabel += " " + argumentStack.current();
        currentCommandParts = new ArrayList<>(command.getParts());
        partsIterator = currentCommandParts.listIterator();
        partsLeft = currentCommandParts.size();
    }

    public ParseResult parse() throws CommandParseException, CommandNotFound {
        Command command = nextAsCommand();
        useCommand(command);

        if (stopParse) {
            throw new CommandParseException("STOPPED_PARSING");
        }

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
            if (stopParse) {
                throw new CommandParseException("STOPPED_PARSING");
            }

            CommandPart partToBind = nextUnboundPart();

            if (partToBind instanceof SubCommandPart) {
                parseSubCommand(partToBind);
            } else if (partToBind instanceof ArgumentPart) {
                parseArgument(partToBind);
            } else if (partToBind instanceof InjectedValuePart) {
                parseInjectedPart(partToBind);
            } else if (partToBind instanceof CommandPartParser) {
                CommandPartParser partParser = (CommandPartParser) partToBind;

                partParser.parse(partToBind, new ParsingContextData(this));
            } else {
                throw new CommandParseException("Invalid part type provided! Type: " + partToBind.getClass().getSimpleName());
            }

        }

        return new ParseResultData(commandLabel.trim(), argumentStack.getBacking(), commandExecutionPath, bindings, valueBindings);
    }

    public void stopParse() {
        this.stopParse = true;
    }

    public List<String> getUsedArguments(StackSnapshot snapshot, int usedArguments) throws NoMoreArgumentsException {
        List<String> arguments = new ArrayList<>();

        while (snapshot.hasNext() && usedArguments > 0) {
            arguments.add(snapshot.next());
            usedArguments--;
        }

        return arguments;
    }


    public int getPartsLeft() {
        return partsLeft;
    }

    public int calculateNeededArgs() {
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

    public int calculateAllNeededArgs() {
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

    public int calculateOptionalArgumentsToBound() {
        return argumentStack.getArgumentsLeft() - neededArguments;
    }

    public void decrementArguments(CommandPart part, int usedArguments) {
        if (part.isRequired()) {
            neededArguments -= usedArguments;
        } else {
            optionalArgumentsToBound -= usedArguments;
        }
        allNeededArguments -= usedArguments;
    }

    public boolean hasSubCommand() {
        return hasSubCommand;
    }

    public int getNeededArguments() {
        return neededArguments;
    }

    public int getAllNeededArguments() {
        return allNeededArguments;
    }

    public int getOptionalArgumentsToBound() {
        return optionalArgumentsToBound;
    }

    /**
     * Adds the specified part as the last part to parse
     *
     * @param partToAdd The part to add
     * @throws CommandParseException If with the addition of this part an infinite part gets on an invalid state(infinite part before required part)
     */
    public void addPart(CommandPart partToAdd) throws CommandParseException {
        currentCommandParts.add(partToAdd);
        try {
            checkForInvalidInfiniteParts();
        } catch (CommandParseException ex) {
            currentCommandParts.remove(currentCommandParts.size() - 1);

            throw ex;
        }

        partsLeft++;

        hasSubCommand = currentCommandParts.subList(partsIterator.nextIndex(), currentCommandParts.size()).stream().anyMatch(part -> part instanceof SubCommandPart && part.isRequired());
        neededArguments = calculateNeededArgs();
        allNeededArguments = calculateAllNeededArgs();
        optionalArgumentsToBound = argumentStack.getArgumentsLeft() - neededArguments;
    }

    /**
     * Adds the specified part as the next part to parse
     *
     * @param partToAdd The part to use
     * @throws CommandParseException If with the addition of this part an infinite part gets on an invalid state(infinite part before required part)
     */
    public void usePart(CommandPart partToUse) throws CommandParseException {
        partsIterator.add(partToUse);
        try {
            checkForInvalidInfiniteParts();
        } catch (CommandParseException ex) {
            partsIterator.next();
            partsIterator.remove();
            partsIterator.previous();

            throw ex;
        }

        partsLeft++;

        hasSubCommand = currentCommandParts.subList(partsIterator.nextIndex(), currentCommandParts.size()).stream().anyMatch(part -> part instanceof SubCommandPart && part.isRequired());
        neededArguments = calculateNeededArgs();
        allNeededArguments = calculateAllNeededArgs();
        optionalArgumentsToBound = argumentStack.getArgumentsLeft() - neededArguments;
    }

    /**
     *  Removes from part's list the next available part
     */
    public void removeNextPart() {
        partsIterator.next();
        partsIterator.remove();
        partsIterator.previous();

        partsLeft--;

        hasSubCommand = currentCommandParts.subList(partsIterator.nextIndex(), currentCommandParts.size()).stream().anyMatch(part -> part instanceof SubCommandPart && part.isRequired());
        neededArguments = calculateNeededArgs();
        allNeededArguments = calculateAllNeededArgs();
        optionalArgumentsToBound = argumentStack.getArgumentsLeft() - neededArguments;
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

        if (stopParse) {
            return;
        }

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

        ParameterProvider.Result<?> object;

        try {
            object = provider.transform(argumentsToUse, namespaceAccesor, part);
        } catch (NoMoreArgumentsException ex) {
            if (part.isRequired()) {
                String message = commandManager.getI18n().getMessage(Message.MISSING_ARGUMENT, commandExecutionPath, namespaceAccesor);

                if (message == null) {
                    message = "Missing arguments for required part %s minimum arguments required: %s";
                }

                commandManager.getMessenger().sendMessage(namespaceAccesor, message, part.getName(), neededArguments + "");

                throw new CommandUsageException(UsageBuilder.getUsageForCommand(null, currentCommand, commandLabel));
            }

            if (partsLeft > 0) {
                argumentStack.applySnapshot(start);
            }

            return;
        }

        int usedArguments = oldArgumentsLeft - argumentStack.getArgumentsLeft();

        decrementArguments(part, usedArguments);
        bindPart(part, getUsedArguments(start, usedArguments));

        valueBindings.put(part, unwrapObject(object, part).orElse(null));
    }

    private Optional<?> unwrapObject(ParameterProvider.Result<?> result, CommandPart part) throws CommandParseException {
        Optional<?> providedObject = result.getResultObject();
        Optional<String> message = result.getMessage();
        Optional<Exception> lastError = result.lastError();

        if (!providedObject.isPresent()) {
            if (lastError.isPresent()) {
                throw new CommandParseException("An exception occurred while parsing the part " + part.getName() + " argument!", lastError.get());
            }

            message.map(s -> {
                if (result.isI18nPath()) {
                    return commandManager.getI18n().getMessage(s, namespaceAccesor);
                }

                return s;
            }).ifPresent(s -> {
                commandManager.getMessenger().sendMessage(namespaceAccesor, s, result.getMessageParameters().toArray());
            });
            stopParse();

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

        if (!argumentStack.hasNext()) {
            if (partToBind.isRequired()) {
                String message = commandManager.getI18n().getMessage(Message.MISSING_SUBCOMMAND, commandExecutionPath, namespaceAccesor);

                if (message == null) {
                    message = "Missing argument for required part %s, available values: %s";
                }

                commandManager.getMessenger().sendMessage(namespaceAccesor, message, partToBind.getName(), availableValuesString);

                throw new CommandUsageException(UsageBuilder.getUsageForCommand(null, currentCommand, commandLabel));
            }

            return;
        }

        String argument = argumentStack.next();

        Command command = availableValues.get(argument.toLowerCase());

        if (command == null) {
            String message = commandManager.getI18n().getMessage(Message.INVALID_SUBCOMMAND, commandExecutionPath, namespaceAccesor);

            if (message == null) {
                message = "Invalid sub-command, valid values: %s";
            }

            commandManager.getMessenger().sendMessage(namespaceAccesor, message, availableValuesString);

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

    static class ParsingContextData implements ParsingContext {

        private CommandLineParser parser;

        public ParsingContextData(CommandLineParser parser) {
            this.parser = parser;
        }

        @Override
        public CommandLineParser getParser() {
            return parser;
        }

        @Override
        public ArgumentStack getStack() {
            return parser.argumentStack;
        }

        @Override
        public NamespaceAccesor getNamespace() {
            return parser.namespaceAccesor;
        }

        @Override
        public List<Command> getExecutionPath() {
            return parser.commandExecutionPath;
        }

        @Override
        public Command getCurrentCommand() {
            return parser.currentCommand;
        }

        @Override
        public ParameterProviderRegistry getRegistry() {
            return parser.providerRegistry;
        }

        @Override
        public CommandManager getCommandManager() {
            return parser.commandManager;
        }
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
