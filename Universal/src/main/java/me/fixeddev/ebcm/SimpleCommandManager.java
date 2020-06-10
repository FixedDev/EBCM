package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoPermissionException;
import me.fixeddev.ebcm.i18n.DefaultI18n;
import me.fixeddev.ebcm.i18n.I18n;
import me.fixeddev.ebcm.i18n.Message;
import me.fixeddev.ebcm.input.InputTokenizer;
import me.fixeddev.ebcm.internal.CommandLineParser;
import me.fixeddev.ebcm.internal.namespace.SimpleCommandContext;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.part.ArgumentPart;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.part.FlagPart;
import me.fixeddev.ebcm.part.SubCommandPart;
import me.fixeddev.ebcm.util.UsageBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The default implementation for {@link CommandManager} using a HashMap for the internal commandMap
 * And delegating the parse calls to the {@link CommandLineParser}
 *
 * @implNote This class is not threadsafe, we can't ensure that registering/executing commands on more than 1 thread concurrently works correctly
 */
public class SimpleCommandManager implements CommandManager {
    private Map<String, Command> commandMap;

    private Authorizer authorizer;
    private Messenger messenger;
    private ParameterProviderRegistry registry;
    private InputTokenizer tokenizer;
    private I18n i18n;

    public SimpleCommandManager(Authorizer authorizer, Messenger messenger, ParameterProviderRegistry registry) {
        this.authorizer = authorizer;
        this.messenger = messenger;
        this.registry = registry;
        this.i18n = new DefaultI18n();

        commandMap = new HashMap<>();
    }

    public SimpleCommandManager(ParameterProviderRegistry registry) {
        this((namespace, permission) -> true, (namespaceAccessor, message, parameters) -> {
            System.out.println(String.format(message, parameters));
        }, registry);
    }

    public SimpleCommandManager() {
        this(ParameterProviderRegistry.createRegistry());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerCommand(Command command) {
        CommandData data = command.getData();

        if (commandMap.containsKey(data.getName())) {
            throw new IllegalArgumentException("A command with the name " + data.getName() + " is already registered!");
        }

        commandMap.put(data.getName().toLowerCase(), command);

        data.getAliases().forEach(alias -> {
            commandMap.putIfAbsent(alias.toLowerCase(), command);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerCommands(List<Command> commandList) {
        for (Command command : commandList) {
            registerCommand(command);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String commandName) {
        return commandMap.containsKey(commandName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParameterProviderRegistry getProviderRegistry() {
        return registry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuthorizer(Authorizer authorizer) {
        if (authorizer == null) {
            throw new IllegalArgumentException("Trying to set a null authorizer!");
        }

        this.authorizer = authorizer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessenger(Messenger messenger) {
        if (messenger == null) {
            throw new IllegalArgumentException("Trying to set a null messenger!");
        }

        this.messenger = messenger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputTokenizer getInputTokenizer() {
        return tokenizer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInputTokenizer(InputTokenizer tokenizer) {
        if (tokenizer == null) {
            throw new IllegalArgumentException("Trying to set a null input tokenizer!");
        }

        this.tokenizer = tokenizer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public I18n getI18n() {
        return i18n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setI18n(I18n i18n) {
        if (i18n == null) {
            throw new IllegalArgumentException("Trying to set a null i18n instance!");
        }

        this.i18n = i18n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Command> getCommand(String commandName) {
        return Optional.ofNullable(commandMap.get(commandName.toLowerCase()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException {
        ParseResult result;

        try {
            result = parse(accessor, arguments);
        } catch (CommandParseException e) {
            if (e.getMessage().equals("STOPPED_PARSING")) {
                return false;
            }

            throw e;
        } catch (CommandNotFound ex) {
            return false;
        }

        CommandContext context = new SimpleCommandContext(accessor, result);

        Command toExecute = result.getCommandToExecute();
        CommandAction action = toExecute.getAction();

        boolean usage = false;

        try {
            if (!action.execute(context)) {
                usage = true;
            }
        } catch (Exception ex) {
            throw new CommandException("An exception occurred while executing the command", ex);
        }

        if (usage) {
            String message = i18n.getMessage(Message.COMMAND_USAGE, result.getCommandExecutionPath(), accessor);

            if (message == null) {
                message = "Usage: %1$s";
            }

            messenger.sendMessage(accessor, message, UsageBuilder.getUsageForCommand(result.getMainCommand(), toExecute, result.getLabel()));
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(NamespaceAccesor accessor, String line) throws CommandParseException, CommandException {
        return execute(accessor, tokenizer.tokenize(line));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSuggestions(NamespaceAccesor accessor, List<String> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<Command> optionalCommand = getCommand(arguments.get(0));

        if (!optionalCommand.isPresent()) {
            return Collections.emptyList();
        }

        arguments.remove(0);

        Command command = optionalCommand.get();

        if (!authorizer.isAuthorized(accessor, command.getPermission())) {
            return Collections.emptyList();
        }

        List<CommandPart> parts = command.getParts();
        arguments = parseFlags(arguments, parts);

        int argumentsLeft = arguments.size();
        int lastArgumentsLeft;

        CommandPart partToComplete = null;
        String startsWith = "";

        List<CommandPart> commandParts = command.getParts();

        ListIterator<CommandPart> partsIterator = commandParts.listIterator();
        while (partsIterator.hasNext()) {
            CommandPart part = partsIterator.next();

            if (part instanceof ArgumentPart) {
                ArgumentPart argumentPart = (ArgumentPart) part;
                lastArgumentsLeft = argumentsLeft;
                argumentsLeft -= argumentPart.getConsumedArguments();

                if (argumentsLeft <= 0) {
                    if (lastArgumentsLeft > 0) {
                        int i = argumentPart.getConsumedArguments() + argumentsLeft;

                        if (i == 0) {
                            startsWith = arguments.get(arguments.size() - 1);
                        } else {
                            startsWith = String.join(" ", arguments.subList(arguments.size() - i, arguments.size()));
                        }
                    }

                    partToComplete = part;
                    break;
                }
            }

            if (part instanceof SubCommandPart) {
                lastArgumentsLeft = argumentsLeft;
                argumentsLeft--;

                if (argumentsLeft <= 0) {
                    partToComplete = part;
                    startsWith = arguments.get(arguments.size() - 1);

                    break;
                }

                if (!partsIterator.hasNext()) {
                    Map<String, Command> availableValues = new HashMap<>();

                    for (Command subCommand : ((SubCommandPart) part).getCommandsToCall()) {
                        availableValues.put(subCommand.getData().getName(), subCommand);

                        for (String value : command.getData().getAliases()) {
                            availableValues.put(value.toLowerCase(), subCommand);
                        }
                    }

                    String lastArgument = arguments.get(arguments.size() - argumentsLeft - 1);

                    Command subCommand = availableValues.get(lastArgument.toLowerCase());

                    if (subCommand == null) {
                        continue;
                    }

                    if (authorizer.isAuthorized(accessor, subCommand.getPermission())) {
                        continue;
                    }

                    parts = subCommand.getParts();
                    partsIterator = parts.listIterator();
                }
            }
        }

        if (partToComplete == null) {
            return Collections.emptyList();
        }

        if (partToComplete instanceof ArgumentPart) {
            ArgumentPart part = (ArgumentPart) partToComplete;
            SuggestionProvider provider = part.getSuggestionProvider().orElse(getProviderRegistry().getParameterProvider(part.getArgumentType()));

            if (provider == null) {
                return Collections.emptyList();
            }

            return provider.getSuggestions(startsWith);
        }

        SubCommandPart part = (SubCommandPart) partToComplete;

        return getSubCommands(accessor, part, startsWith);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSuggestions(NamespaceAccesor accessor, String line) throws NoPermissionException {
        return getSuggestions(accessor, tokenizer.tokenize(line));
    }

    private List<String> getSubCommands(NamespaceAccesor accesor, SubCommandPart part, String startsWith) {
        List<String> availableValues = new ArrayList<>();

        for (Command command : part.getCommandsToCall()) {
            if (!authorizer.isAuthorized(accesor, command.getPermission())) {
                continue;
            }

            String name = command.getData().getName();

            if (name.startsWith(startsWith)) {
                availableValues.add(name.toLowerCase());
            }

            for (String alias : command.getData().getAliases()) {
                if (alias.startsWith(startsWith)) {
                    availableValues.add(alias.toLowerCase());
                }
            }
        }

        return availableValues;
    }

    private List<String> parseFlags(List<String> arguments, List<CommandPart> parts) {
        Map<Character, FlagPart> flagParts = parts.stream()
                .filter(part -> part instanceof FlagPart)
                .map(part -> (FlagPart) part).collect(Collectors.toMap(FlagPart::getFlagChar, Function.identity()));

        List<String> newArguments = new ArrayList<>();

        boolean ignore = false;

        for (String argument : arguments) {
            // Disable the parsing of the next flags
            if ("--".equals(argument)) {
                ignore = true;
                break;
            }

            if (argument.startsWith("-") && argument.length() == 2 && !ignore) {
                char flagChar = argument.charAt(1);
                FlagPart part = flagParts.get(flagChar);

                if (part != null && !ignore) {
                    continue;
                }
            }

            newArguments.add(argument);
        }

        parts.removeAll(flagParts.values());

        return newArguments;
    }

    /**
     * Delegates the parse phase to the {@link CommandLineParser} class
     * {@inheritDoc}
     */
    @Override
    public ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound {
        CommandLineParser parser = new CommandLineParser(arguments, accessor, this);

        return parser.parse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParseResult parse(NamespaceAccesor accessor, String line) throws CommandParseException, CommandNotFound {
        return parse(accessor, tokenizer.tokenize(line));
    }
}
