package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;

import java.util.List;
import java.util.Optional;

public interface CommandManager {

    void registerCommand(Command command);

    boolean exists(String commandName);

    ParameterProviderRegistry getProviderRegistry();

    Optional<Command> getCommand(String commandName);

    boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException;

    ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound;
}
