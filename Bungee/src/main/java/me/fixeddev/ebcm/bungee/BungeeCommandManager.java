package me.fixeddev.ebcm.bungee;

import me.fixeddev.ebcm.Authorizer;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.CommandUsageHandler;
import me.fixeddev.ebcm.Messenger;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.ParseResult;
import me.fixeddev.ebcm.SimpleCommandManager;
import me.fixeddev.ebcm.bungee.parameter.provider.BungeeModule;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.i18n.I18n;
import me.fixeddev.ebcm.input.InputTokenizer;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BungeeCommandManager implements CommandManager {

    public static final String SENDER_NAMESPACE = "SENDER";

    private CommandManager parent;
    private Plugin plugin;

    private Map<String, BungeeCommandWrapper> wrapperMap;

    public BungeeCommandManager(CommandManager parent, Plugin plugin) {
        this.parent = parent;
        this.plugin = plugin;

        wrapperMap = new HashMap<>();
    }

    public BungeeCommandManager(Plugin plugin) {
        this(new SimpleCommandManager(), plugin);

        setAuthorizer(new BungeeAuthorizer());
        setMessenger(new BungeeMessenger());

        getProviderRegistry().installModule(new BungeeModule());
        setI18n(new BungeeDefaultI18n());
    }

    @Override
    public void registerCommand(Command command) {
        parent.registerCommand(command);
        BungeeCommandWrapper bungeeCommand = new BungeeCommandWrapper(command, this);
        wrapperMap.put(command.getData().getName(), bungeeCommand);

        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, bungeeCommand);
    }

    @Override
    public void registerCommands(List<Command> commandList) {
        for (Command command : commandList) {
            registerCommand(command);
        }
    }

    @Override
    public void unregisterCommand(Command command) {
        parent.unregisterCommand(command);

        BungeeCommandWrapper wrapper = wrapperMap.get(command.getData().getName());
        if (wrapper != null) {
            ProxyServer.getInstance().getPluginManager().unregisterCommand(wrapper);
        }

    }

    @Override
    public void unregisterCommands(List<Command> commands) {
        parent.unregisterCommands(commands);
    }

    @Override
    public void unregisterAll() {
        parent.unregisterAll();
    }

    @Override
    public Set<Command> getCommands() {
        return parent.getCommands();
    }

    @Override
    public boolean exists(String commandName) {
        return parent.exists(commandName);
    }

    @Override
    public CommandUsageHandler getUsageHandler() {
        return parent.getUsageHandler();
    }

    @Override
    public void setUsageHandler(CommandUsageHandler usageHandler) {
        parent.setUsageHandler(usageHandler);
    }

    @Override
    public ParameterProviderRegistry getProviderRegistry() {
        return parent.getProviderRegistry();
    }

    @Override
    public Authorizer getAuthorizer() {
        return parent.getAuthorizer();
    }

    @Override
    public void setAuthorizer(Authorizer authorizer) {
        parent.setAuthorizer(authorizer);
    }

    @Override
    public Messenger getMessenger() {
        return parent.getMessenger();
    }

    @Override
    public void setMessenger(Messenger messenger) {
        parent.setMessenger(messenger);
    }

    @Override
    public InputTokenizer getInputTokenizer() {
        return parent.getInputTokenizer();
    }

    @Override
    public void setInputTokenizer(InputTokenizer tokenizer) {
        parent.setInputTokenizer(tokenizer);
    }

    @Override
    public I18n getI18n() {
        return parent.getI18n();
    }

    @Override
    public void setI18n(I18n i18n) {
        parent.setI18n(i18n);
    }


    @Override
    public Optional<Command> getCommand(String commandName) {
        return parent.getCommand(commandName);
    }

    @Override
    public boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException {
        return parent.execute(accessor, arguments);
    }

    @Override
    public boolean execute(NamespaceAccesor accessor, String line) throws CommandParseException, CommandException {
        return parent.execute(accessor, line);
    }

    @Override
    public List<String> getSuggestions(NamespaceAccesor accessor, List<String> arguments) {
        return parent.getSuggestions(accessor, arguments);
    }

    @Override
    public List<String> getSuggestions(NamespaceAccesor accessor, String line) {
        return parent.getSuggestions(accessor, line);
    }

    @Override
    public ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound {
        return parent.parse(accessor, arguments);
    }

    @Override
    public ParseResult parse(NamespaceAccesor accessor, String line) throws CommandParseException, CommandNotFound {
        return parent.parse(accessor, line);
    }

}
