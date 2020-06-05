package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandNotFound;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoPermissionException;
import me.fixeddev.ebcm.i18n.I18n;
import me.fixeddev.ebcm.input.InputTokenizer;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;

import java.util.List;
import java.util.Optional;

public interface CommandManager {

    void registerCommand(Command command);

    void registerCommands(List<Command> commandList);

    boolean exists(String commandName);

    ParameterProviderRegistry getProviderRegistry();

    Authorizer getAuthorizer();

    void setAuthorizer(Authorizer authorizer);

    Messenger getMessenger();

    void setMessenger(Messenger messenger);

    InputTokenizer getInputTokenizer();

    void setInputTokenizer(InputTokenizer tokenizer);

    I18n getI18n();

    void setI18n(I18n i18n);

    Optional<Command> getCommand(String commandName);

    boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException;

    boolean execute(NamespaceAccesor accessor, String line) throws CommandParseException, CommandException;

    List<String> getSuggestions(NamespaceAccesor accessor, List<String> arguments) throws NoPermissionException;

    List<String> getSuggestions(NamespaceAccesor accessor, String line) throws NoPermissionException;

    ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound;

    ParseResult parse(NamespaceAccesor accessor, String line) throws CommandParseException, CommandNotFound;
}
