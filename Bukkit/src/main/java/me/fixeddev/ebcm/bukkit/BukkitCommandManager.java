package me.fixeddev.ebcm.bukkit;

import me.fixeddev.ebcm.Authorizer;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.Messager;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.ParseResult;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoPermissionException;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class BukkitCommandManager implements CommandManager {
    public static final String SENDER_NAMESPACE = "SENDER";
    private CommandManager delegate;
    private CommandMap bukkitCommandMap;
    private String fallbackPrefix;


    public BukkitCommandManager(CommandManager delegate, String fallbackPrefix) {
        this.delegate = delegate;
        this.fallbackPrefix = fallbackPrefix;

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            bukkitCommandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get command map: ", ex);
        }
    }

    public void registerCommand(Command command) {
        delegate.registerCommand(command);

        org.bukkit.command.Command bukkitCommand = new BukkitCommandWrapper(command, this);
        bukkitCommandMap.register(fallbackPrefix, bukkitCommand);
    }

    public void registerCommands(List<Command> commandList) {
        for (Command command : commandList) {
            registerCommand(command);
        }
    }

    public boolean exists(String commandName) {
        return delegate.exists(commandName);
    }

    public ParameterProviderRegistry getProviderRegistry() {
        return delegate.getProviderRegistry();
    }

    @Override
    public Authorizer getAuthorizer() {
        return delegate.getAuthorizer();
    }

    @Override
    public Messager getMessager() {
        return delegate.getMessager();
    }

    public Optional<Command> getCommand(String commandName) {
        return delegate.getCommand(commandName);
    }

    public boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException {
        return delegate.execute(accessor, arguments);
    }

    @Override
    public List<String> getSuggestions(NamespaceAccesor accessor, List<String> arguments) throws NoPermissionException {
        return delegate.getSuggestions(accessor, arguments);
    }

    public ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound {
        return delegate.parse(accessor, arguments);
    }

}
