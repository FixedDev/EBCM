package me.fixeddev.ebcm.bukkit;

import me.fixeddev.ebcm.Authorizer;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.Messenger;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.ParseResult;
import me.fixeddev.ebcm.SimpleCommandManager;
import me.fixeddev.ebcm.bukkit.parameter.provider.BukkitModule;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoPermissionException;
import me.fixeddev.ebcm.i18n.I18n;
import me.fixeddev.ebcm.input.InputTokenizer;
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

    public BukkitCommandManager(String fallbackPrefix) {
        this(new SimpleCommandManager(), fallbackPrefix);

        setAuthorizer(new BukkitAuthorizer());
        setMessenger(new BukkitMessenger());
        getProviderRegistry().installModule(new BukkitModule());
        setI18n(new BukkitDefaultI18n());
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

    @Override
    public boolean exists(String commandName) {
        return delegate.exists(commandName);
    }

    @Override
    public ParameterProviderRegistry getProviderRegistry() {
        return delegate.getProviderRegistry();
    }

    @Override
    public Authorizer getAuthorizer() {
        return delegate.getAuthorizer();
    }

    @Override
    public void setAuthorizer(Authorizer authorizer) {
        delegate.setAuthorizer(authorizer);
    }

    @Override
    public Messenger getMessenger() {
        return delegate.getMessenger();
    }

    @Override
    public void setMessenger(Messenger messenger) {
        delegate.setMessenger(messenger);
    }

    @Override
    public InputTokenizer getInputTokenizer() {
        return delegate.getInputTokenizer();
    }

    @Override
    public void setInputTokenizer(InputTokenizer tokenizer) {
        delegate.setInputTokenizer(tokenizer);
    }

    @Override
    public I18n getI18n() {
        return delegate.getI18n();
    }

    @Override
    public void setI18n(I18n i18n) {
        delegate.setI18n(i18n);
    }


    @Override
    public Optional<Command> getCommand(String commandName) {
        return delegate.getCommand(commandName);
    }

    @Override
    public boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException {
        return delegate.execute(accessor, arguments);
    }

    @Override
    public boolean execute(NamespaceAccesor accessor, String line) throws CommandParseException, CommandException {
        return delegate.execute(accessor, line);
    }

    @Override
    public List<String> getSuggestions(NamespaceAccesor accessor, List<String> arguments) throws NoPermissionException {
        return delegate.getSuggestions(accessor, arguments);
    }

    @Override
    public List<String> getSuggestions(NamespaceAccesor accessor, String line) throws NoPermissionException {
        return delegate.getSuggestions(accessor, line);
    }

    @Override
    public ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound {
        return delegate.parse(accessor, arguments);
    }

    @Override
    public ParseResult parse(NamespaceAccesor accessor, String line) throws CommandParseException, CommandNotFound {
        return delegate.parse(accessor, line);
    }


}
