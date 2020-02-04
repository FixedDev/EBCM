package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;

import java.util.List;
import java.util.Optional;

public interface CommandManager {

    void registerCommand(Command command);

    void registerCommands(List<Command> commandList);

    boolean exists(String commandName);

    ParameterProviderRegistry getProviderRegistry();

    Authorizer getAuthorizer();

    Optional<Command> getCommand(String commandName);

    boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException;

    List<String> getSuggestions(NamespaceAccesor accessor, List<String> arguments) throws NoMoreArgumentsException;
    
    ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound;
}
