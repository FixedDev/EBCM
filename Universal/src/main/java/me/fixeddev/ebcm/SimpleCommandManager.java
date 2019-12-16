package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.CommandUsageException;
import me.fixeddev.ebcm.internal.CommandLineParser;
import me.fixeddev.ebcm.internal.namespace.SimpleCommandContext;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.util.UsageBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            throw new CommandUsageException(toExecute.getPermissionMessage());
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
            throw new CommandUsageException(UsageBuilder.getUsageForCommand(toExecute, result.getLabel()));
        }

        return true;
    }

    @Override
    public ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound {
        CommandLineParser parser = new CommandLineParser(arguments, accessor, this);

        return parser.parse();
    }
}
