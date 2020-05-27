package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.internal.CommandLineParser;
import me.fixeddev.ebcm.internal.Messages;
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

public class SimpleCommandManager implements CommandManager {
    private Map<String, Command> commandMap;

    private Authorizer authorizer;
    private Messager messager;
    private ParameterProviderRegistry registry;

    public SimpleCommandManager(Authorizer authorizer, Messager messager, ParameterProviderRegistry registry) {
        this.authorizer = authorizer;
        this.messager = messager;
        this.registry = registry;


        commandMap = new HashMap<>();
    }

    public SimpleCommandManager(ParameterProviderRegistry registry) {
        this((namespace, permission) -> true, (namespaceAccesor, messageId, message, parameters) -> {
            System.out.println(String.format(message, (Object[]) parameters));
        }, registry);
    }

    public SimpleCommandManager() {
        this(ParameterProviderRegistry.createRegistry());
    }

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

    @Override
    public void registerCommands(List<Command> commandList) {
        for (Command command : commandList) {
            registerCommand(command);
        }
    }

    @Override
    public boolean exists(String commandName) {
        return commandMap.containsKey(commandName);
    }

    @Override
    public ParameterProviderRegistry getProviderRegistry() {
        return registry;
    }

    @Override
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    @Override
    public Messager getMessager() {
        return messager;
    }

    @Override
    public Optional<Command> getCommand(String commandName) {
        return Optional.ofNullable(commandMap.get(commandName.toLowerCase()));
    }

    @Override
    public boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException {
        ParseResult result;

        try {
            result = parse(accessor, arguments);
        } catch (CommandNotFound ex) {
            return false;
        }

        CommandContext context = new SimpleCommandContext(accessor, result);

        Command toExecute = result.getCommandToExecute();

        if (!authorizer.isAuthorized(accessor, toExecute.getPermission())) {
            messager.sendMessage(accessor, Messages.COMMAND_NO_PERMISSIONS.getId(), toExecute.getPermissionMessage());

            return true;
        }

        CommandAction action = toExecute.getAction();

        boolean usage = false;

        try {
            if (!action.execute(context)) {
                usage = true;
            }
        } catch (CommandException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CommandException("An exception occurred while executing the command", ex);
        }

        if (usage) {
            messager.sendMessage(accessor, Messages.COMMAND_USAGE.getId(), "Usage: %1$s", UsageBuilder.getUsageForCommand(result.getMainCommand(), toExecute, result.getLabel()));
        }

        return true;
    }

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
            if (argument.startsWith("-") && argument.length() == 2 && !ignore) {
                char flagChar = argument.charAt(1);
                FlagPart part = flagParts.get(flagChar);

                if (part != null && !ignore) {
                    continue;
                }
            }

            // Disable the parsing of the next flags
            if ("--".equals(argument)) {
                ignore = true;
                break;
            }

            newArguments.add(argument);
        }

        parts.removeAll(flagParts.values());

        return newArguments;
    }


    @Override
    public ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound {
        CommandLineParser parser = new CommandLineParser(arguments, accessor, this);

        return parser.parse();
    }
}
