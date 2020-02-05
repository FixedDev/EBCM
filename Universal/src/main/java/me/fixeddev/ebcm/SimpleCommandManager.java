package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.CommandUsageException;
import me.fixeddev.ebcm.exception.NoPermissionException;
import me.fixeddev.ebcm.internal.CommandLineParser;
import me.fixeddev.ebcm.internal.namespace.SimpleCommandContext;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleCommandManager implements CommandManager {
    private Map<String, Command> commandMap;

    private Authorizer authorizer;
    private ParameterProviderRegistry registry;

    public SimpleCommandManager(Authorizer authorizer, ParameterProviderRegistry registry) {
        this.authorizer = authorizer;
        this.registry = registry;

        commandMap = new HashMap<>();
    }

    public SimpleCommandManager(ParameterProviderRegistry registry) {
        this((namespace, permission) -> true, registry);
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

        commandMap.put(data.getName(), command);

        data.getAliases().forEach(alias -> {
            commandMap.putIfAbsent(alias, command);
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
    public Optional<Command> getCommand(String commandName) {
        return Optional.ofNullable(commandMap.get(commandName));
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
            throw new NoPermissionException(toExecute.getPermissionMessage());
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
            throw new CommandUsageException(UsageBuilder.getUsageForCommand(result.getMainCommand(), toExecute, result.getLabel()));
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

        List<CommandPart> parts = command.getParts();
        arguments = parseFlags(arguments, parts);

        int argumentsLeft = arguments.size();
        int lastArgumentsLeft;

        CommandPart partToComplete = null;
        String startsWith = "";

        for (CommandPart part : command.getParts()) {
            if (part instanceof ArgumentPart) {
                ArgumentPart argumentPart = (ArgumentPart) part;
                lastArgumentsLeft = argumentsLeft;
                argumentsLeft -= argumentPart.getConsumedArguments();

                if (argumentsLeft <= 0) {
                    if(lastArgumentsLeft > 0){
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
            }
        }

        if (partToComplete == null) {
            return Collections.emptyList();
        }

        if (partToComplete instanceof ArgumentPart) {
            ArgumentPart part = (ArgumentPart) partToComplete;

            ParameterProvider<?> provider = getProviderRegistry().getParameterProvider(part.getArgumentType());

            if (provider == null) {
                return Collections.emptyList();
            }

            return provider.getSuggestions(startsWith);
        }

        SubCommandPart part = (SubCommandPart) partToComplete;

        return getSubCommands(part, startsWith);
    }

    private List<String> getSubCommands(SubCommandPart part, String startsWith) {
        List<String> availableValues = new ArrayList<>();

        for (Command command : part.getCommandsToCall()) {
            String name = command.getData().getName();

            if(name.startsWith(startsWith)){
                availableValues.add(name.toLowerCase());
            }

            for (String alias : command.getData().getAliases()) {
                if(alias.startsWith(startsWith)){
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
