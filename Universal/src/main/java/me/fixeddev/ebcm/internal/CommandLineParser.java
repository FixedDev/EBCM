package me.fixeddev.ebcm.internal;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.ParseResult;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.CommandUsageException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.part.ArgumentPart;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.part.FlagPart;
import me.fixeddev.ebcm.part.SubCommandPart;
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

    private Command rootCommand;
    private String commandLabel;

    // Per command fields start
    private List<CommandPart> currentCommandParts;
    private Command currentCommand;

    private int partsLeft;
    private ListIterator<CommandPart> partsIterator;
    private CommandPart currentPart;
    // Per command fields end

    private int argumentsLeft;
    private ListIterator<String> argumentsIterator;
    private List<String> allArguments;
    private String currentArgument;

    private List<ParseResult.ParameterBinding> bindings;
    private Map<CommandPart, Object> valueBindings;

    private NamespaceAccesor namespaceAccesor;

    private CommandManager commandManager;

    private ParameterProviderRegistry providerRegistry;


    public CommandLineParser(List<String> argumentsLine, NamespaceAccesor namespaceAccesor, CommandManager commandManager) {
        commandLabel = "";
        allArguments = argumentsLine;

        argumentsIterator = argumentsLine.listIterator();
        argumentsLeft = argumentsLine.size();

        bindings = new ArrayList<>();
        valueBindings = new LinkedHashMap<>();

        this.commandManager = commandManager;
        this.namespaceAccesor = namespaceAccesor;
        providerRegistry = commandManager.getProviderRegistry();
    }

    public String nextArgument() throws CommandParseException {
        if (!argumentsIterator.hasNext()) {
            throw new NoMoreArgumentsException(allArguments.size(), argumentsIterator.nextIndex() - 1);
        }

        argumentsLeft--;
        return (currentArgument = argumentsIterator.next());
    }

    public String currentArgument() throws CommandParseException {
        if (currentArgument == null) {
            throw new CommandParseException("You must advance at least one argument before calling this method!");
        }

        return currentArgument;
    }

    public boolean hasNextArgument() {
        return argumentsIterator.hasNext();
    }

    public CommandPart nextUnboundPart() throws CommandParseException {
        if (!partsIterator.hasNext()) {
            throw new CommandParseException("There're is not a next part!");
        }

        return (currentPart = partsIterator.next());
    }

    public CommandPart currentUnboundPart() throws CommandParseException {
        if (hasCurrentUnboundPart()) {
            throw new CommandParseException("The're are no more unbound parts left!");
        }

        return currentPart;
    }

    public boolean hasCurrentUnboundPart() {
        return currentPart != null;
    }

    public boolean hasNextUnboundPart() {
        return partsIterator.hasNext();
    }

    public void bindPart(CommandPart part, List<String> arguments) {
        ParseResult.ParameterBinding binding = new ParameterBindingData(arguments, part);

        bindings.add(binding);
    }

    public Command currentAsRootCommand() throws CommandNotFound, CommandParseException {
        if (currentArgument == null) {
            throw new CommandParseException("You must advance at least one argument before calling this method!");
        }

        if (rootCommand != null) {
            throw new CommandParseException("The command was already found, no need to execute this method again!");
        }

        rootCommand = commandManager.getCommand(currentArgument)
                .orElseThrow(() -> new CommandNotFound("The current argument(" + currentArgument + ") can't be found as command!"));

        useCommand(rootCommand);

        return rootCommand;
    }

    public void useCommand(Command command) {
        currentCommand = command;

        commandLabel += " " + currentArgument;
        currentCommandParts = new ArrayList<>(command.getParts());
        partsIterator = currentCommandParts.listIterator();
        partsLeft = currentCommandParts.size();
    }

    private void parseFlags() throws CommandParseException {
        if (!bindings.isEmpty()) {
            throw new CommandParseException("This method can't be called after one part is bound!");
        }

        Map<Character, FlagPart> flagParts = currentCommandParts.stream()
                .filter(part -> part instanceof FlagPart)
                .map(part -> (FlagPart) part).collect(Collectors.toMap(FlagPart::getFlagChar, Function.identity()));

        currentCommandParts.removeAll(flagParts.values());
        partsIterator = currentCommandParts.listIterator();
        partsLeft = currentCommandParts.size();

        for (String argument : new ArrayList<>(allArguments)) {
            if (!argument.startsWith("-") || argument.length() != 2) {
                continue;
            }

            // Disable the parsing of the next flags
            if (argument.equals("--")) {
                allArguments.remove(argument);
                argumentsLeft--;
                break;
            }

            char flagChar = argument.charAt(1);
            FlagPart part = flagParts.get(flagChar);

            // The flag is valid, but it doesn't has a part to be bound
            if (part == null) {
                continue;
            }

            allArguments.remove(argument);
            bindings.add(new ParameterBindingData(argument, part));
            valueBindings.put(part, true);

            // We remove the part, at the end create a value binding for every flag that wasn't
            // in the command line with value false
            flagParts.remove(flagChar);

            argumentsLeft--;
        }

        flagParts.values().forEach(part -> valueBindings.put(part, false));
        argumentsIterator = allArguments.listIterator();
        // Move the cursor one time, to prevent that an argument that was already used is being used again
        argumentsIterator.next();
    }

    public ParseResult parse() throws CommandParseException, CommandNotFound {
        nextArgument();
        currentAsRootCommand();
        parseFlags();

        if (partsLeft <= 0) {
            return new ParseResultData(commandLabel, allArguments, currentCommand, currentCommand, bindings, valueBindings);
        }

        boolean hasSubCommand = currentCommandParts.subList(partsIterator.nextIndex(), currentCommandParts.size()).stream().anyMatch(part -> part instanceof SubCommandPart);
        int neededArguments = calculateNeededArgs();
        int allNeededArguments = calculateAllNeededArgs();
        // Ok, the name of this is not clear
        // But, this variable it's supposed to mean the quantity of optional parts
        // that can be bound before only the required parts can be bound
        int optionalArgumentsToBound = argumentsLeft - neededArguments;

        checkForInvalidInfiniteParts();

        while (hasNextUnboundPart()) {
            CommandPart partToBind = nextUnboundPart();

            if (partToBind instanceof SubCommandPart) {
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
                String usage = UsageBuilder.getUsageForCommand(currentCommand, commandLabel);

                if (!hasNextArgument()) {
                    throw new CommandUsageException("Missing argument for required part " + partToBind.getName()
                            + ", available values: " + availableValuesString + "\n " + usage);
                }

                String argument = nextArgument();
                Command command = availableValues.get(argument.toLowerCase());

                if (command == null) {
                    throw new CommandUsageException("Invalid sub-command, valid values: " + availableValuesString + "\n " + usage);
                }

                useCommand(command);
                hasSubCommand = currentCommandParts.subList(partsIterator.nextIndex(), currentCommandParts.size()).stream().anyMatch(part -> part instanceof SubCommandPart);
                neededArguments = calculateNeededArgs();
                allNeededArguments = calculateAllNeededArgs();

                ParseResult.ParameterBinding parameterBinding = new ParameterBindingData(currentArgument, currentPart);
                bindings.add(parameterBinding);
                continue;
            }

            if (partToBind instanceof ArgumentPart) {
                ArgumentPart part = (ArgumentPart) partToBind;

                ParameterProvider provider = providerRegistry.getParameterProvider(part.getArgumentType());

                if (provider == null) {
                    throw new CommandParseException("Failed to get a provider for the part " + part.getName());
                }

                List<String> argumentsToUse = new ArrayList<>();
                boolean usingDefaults = false;

                // The part is not required so, we check if the part can be bound or not
                if (!part.isRequired() && ((allNeededArguments != -1 && argumentsLeft < neededArguments + optionalArgumentsToBound) || (optionalArgumentsToBound <= 0 || hasSubCommand))) {
                    if (part.getDefaultValues().isEmpty()) {
                        continue;
                    }

                    usingDefaults = true;
                    argumentsToUse = part.getDefaultValues();
                    allNeededArguments = allNeededArguments - part.getConsumedArguments();
                }

                if (!usingDefaults) {
                    for (int i = 0; i < part.getConsumedArguments(); i++) {
                        if (!hasNextArgument()) {
                            throw new CommandUsageException("Missing arguments for required part " + partToBind.getName()
                                    + " minimum arguments required: " + neededArguments + "\n " + UsageBuilder.getUsageForCommand(currentCommand, commandLabel));
                        }

                        String argument = nextArgument();
                        argumentsToUse.add(argument);


                        /*
                         * Don't subtract an argument from needed arguments if this part is not required, because it's not counted there
                         */
                        if (part.isRequired()) {
                            neededArguments--;
                        } else {
                            optionalArgumentsToBound--;
                        }
                        allNeededArguments--;
                    }

                    bindPart(part, argumentsToUse);
                }

                ParameterProvider.Result object = provider.transform(argumentsToUse, namespaceAccesor, part);

                Optional providedObject = object.getResultObject();
                Optional<String> message = object.getMessage();
                Optional<Exception> lastError = object.lastError();

                if (!providedObject.isPresent()) {
                    if (lastError.isPresent()) {
                        throw new CommandParseException("An exception occurred while parsing the part " + part.getName() + " argument!", lastError.get());
                    }

                    if (message.isPresent()) {
                        // TODO: Create a better exception to send messages to the command sender
                        throw new CommandUsageException(message.get());
                    }

                    if (part.isRequired()) {
                        throw new CommandParseException("The provider for the part " + part.getName() + " with type " + part.getArgumentType().getSimpleName() + " provided an empty object for a required part!");
                    }

                    continue;
                }

                valueBindings.put(part, providedObject.get());
            }

        }

        return new ParseResultData(commandLabel, allArguments, rootCommand, currentCommand, bindings, valueBindings);
    }

    private void checkForInvalidInfiniteParts() throws CommandParseException {
        boolean requiredPartFound = false;

        for (CommandPart part : currentCommandParts) {
            if (!(part instanceof ArgumentPart)) {
                continue;
            }

            ArgumentPart argumentPart = (ArgumentPart) part;

            if (argumentPart.isRequired()) {
                requiredPartFound = true;
                continue;
            }

            if (argumentPart.getConsumedArguments() == -1 && requiredPartFound) {
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
        private Command command;
        private Command commandToExecute;
        private List<ParameterBinding> parameterBindings;
        private Map<CommandPart, Object> valueBindings;

        public ParseResultData(String label,
                               List<String> commandLine,
                               Command command,
                               Command commandToExecute,
                               List<ParameterBinding> parameterBindings,
                               Map<CommandPart, Object> valueBindings) {
            this.label = label;
            this.commandLine = commandLine;
            this.command = command;
            this.commandToExecute = commandToExecute;
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
        public Command getMainCommand() {
            return command;
        }

        @Override
        public Command getCommandToExecute() {
            return commandToExecute;
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
