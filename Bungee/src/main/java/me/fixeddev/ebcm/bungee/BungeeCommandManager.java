package me.fixeddev.ebcm.bungee;

import me.fixeddev.ebcm.bungee.parameter.provider.BungeeModule;
import me.fixeddev.ebcm.*;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoPermissionException;
import me.fixeddev.ebcm.input.InputTokenizer;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;
import java.util.Optional;

public class BungeeCommandManager implements CommandManager {

    public static final String SENDER_NAMESPACE = "SENDER";
    private CommandManager parent;
    private Plugin plugin;

    public BungeeCommandManager(CommandManager parent, Plugin plugin) {
        this.parent = parent;
        this.plugin = plugin;

        parent.getProviderRegistry().installModule(new BungeeModule());
    }

    @Override
    public void registerCommand(Command command) {
        parent.registerCommand(command);
        net.md_5.bungee.api.plugin.Command bungeeCommand = new BungeeCommandWrapper(command, this);
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, bungeeCommand);
    }

    @Override
    public void registerCommands(List<Command> commandList) {
        for(Command command : commandList) {
            registerCommand(command);
        }
    }

    @Override
    public boolean exists(String commandName) {
        return parent.exists(commandName);
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
    public Messager getMessager() {
        return parent.getMessager();
    }

    @Override
    public void setMessager(Messager messager) {
        parent.setMessager(messager);
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
    public List<String> getSuggestions(NamespaceAccesor accessor, List<String> arguments) throws NoPermissionException {
        return parent.getSuggestions(accessor, arguments);
    }

    @Override
    public List<String> getSuggestions(NamespaceAccesor accessor, String line) throws NoPermissionException {
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
