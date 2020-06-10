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

/**
 * Manages the command register and command execution.
 * In that way is a CommandRegistry and a CommandDispatcher at the same time, it also manages the parsing phase of the execution of the command
 * And manages the suggestions for the commands, used on Tab Completing on things like CLI applications or Bukkit
 *
 * @implSpec
 */
public interface CommandManager {

    /**
     * Registers the specified {@link Command} into the internal command map
     * Also registers every alias of the command as separate commands with the same value
     *
     * @param command The command to register
     * @throws IllegalArgumentException If the main name is already registered
     */
    void registerCommand(Command command);

    /**
     * Registers the specified {@link List} of {@link Command} into the internal command map doing a for loop and calling {@link CommandManager#registerCommand(Command)} for every command
     *
     * @param commandList The commands to register
     * @throws IllegalArgumentException If the main name of any command is already registered
     */
    void registerCommands(List<Command> commandList);

    /**
     * Checks if the specified {@link String} is the name or alias of a registered command
     * The name checking is case insensitive
     *
     * @param commandName The command name or alias to check against
     * @return if a command with the specified {@code commandName} as main name or alias exists
     */
    boolean exists(String commandName);

    /**
     * The {@link ParameterProviderRegistry} used to parse the command's {@link me.fixeddev.ebcm.parameter.provider.ParameterProvider}'s on this instance of the CommandManager
     *
     * @return The {@link ParameterProviderRegistry} used to parse commands
     */
    ParameterProviderRegistry getProviderRegistry();

    /**
     * The {@link Authorizer} used to check if the actual context of the command is authorized to execute this command
     *
     * @return The {@link Authorizer} used on this CommandManager instance
     */
    Authorizer getAuthorizer();

    /**
     * Changes the {@link Authorizer} used on this instance
     *
     * @param authorizer A non null instance of a {@link Authorizer}
     * @throws IllegalArgumentException If the specified authorizer is null
     */
    void setAuthorizer(Authorizer authorizer);

    /**
     * The {@link Messenger} instance used to send messages to the output stream/executor of this command
     *
     * @return The {@link Messenger} used on this CommandManager instance
     */
    Messenger getMessenger();

    /**
     * Changes the {@link Messenger} used on this instance
     *
     * @param messenger A non null instance of a {@link Messenger}
     * @throws IllegalArgumentException If the specified messenger is null
     */
    void setMessenger(Messenger messenger);

    /**
     * The {@link InputTokenizer} used by this instance on the methods:
     * {@link CommandManager#execute(NamespaceAccesor, String)}
     * {@link CommandManager#parse(NamespaceAccesor, String)}
     * {@link CommandManager#getSuggestions(NamespaceAccesor, String)}
     * to tokenize the command line into a {@link List} of String
     *
     * @return The {@link InputTokenizer} used on this CommandManager instance
     */
    InputTokenizer getInputTokenizer();

    /**
     * Changes the {@link InputTokenizer} used on this instance
     *
     * @param tokenizer A non null instance of an {@link InputTokenizer}
     * @throws IllegalArgumentException If the specified tokenizer is null
     */
    void setInputTokenizer(InputTokenizer tokenizer);

    /**
     * The {@link I18n} instance used to translate/allow modification of the messages send by the API
     *
     * @return The {@link I18n} used on this CommandManager instance
     */
    I18n getI18n();

    /**
     * Changes the {@link I18n} used on this instance
     *
     * @param i18n A non null instance of an {@link I18n}
     * @throws IllegalArgumentException If the specified I18n instance is null
     */
    void setI18n(I18n i18n);

    /**
     * Searches a command with the specified name or alias and wraps it in an {@link Optional} instance
     * This operation is case insensitive
     *
     * @param commandName The command name or alias to check against
     * @return An {@link Optional} Command, absent if a command with that name or alias couldn't be found
     */
    Optional<Command> getCommand(String commandName);

    /**
     * Parses the command by calling the method {@link CommandManager#parse(NamespaceAccesor, List)} and using it's result
     * creates a {@link CommandContext} instance used to execute the command.
     * <p>
     * If the executed {@link Command}'s {@link CommandAction} returns a false value then this method gets the usage for the executed Command and
     * uses the {@link Messenger} to send it to the output stream/executor of the command
     *
     * @param accessor  The {@link NamespaceAccesor} used to inject things into the Command parsing/execution phase
     * @param arguments A {@link List} of arguments including the command used to parse the actual command used and the parameters of that command
     * @return A boolean indicating if a command was executed or not
     * @throws CommandParseException If the parsing phase of the command fails for any reason
     * @throws CommandException      If the execution phase of the command fails for any reason
     */
    boolean execute(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandException;

    /**
     * Partially parses the specified {@link List} of arguments and gets the suggestion for the last argument
     *
     * @param accessor  The {@link NamespaceAccesor} used to inject objects
     * @param arguments A {@link List} of arguments including the command used to parse the actual command used and the parameters of that command
     * @return A {@link List} of {@link String} representing the suggestions for the next argument, empty if the player doesn't has permissions
     */
    List<String> getSuggestions(NamespaceAccesor accessor, List<String> arguments);

    /**
     * Parses the specified {@link List} of arguments, searches the command to execute, and parses every command parameter
     *
     * @param accessor  The {@link NamespaceAccesor} used to inject objects
     * @param arguments A {@link List} of arguments including the command used to parse the used command and parameters of that command
     * @return A {@link ParseResult} indicating the result of the parse and the command to execute
     * @throws CommandParseException If the parsing of the command fails for any reason
     * @throws CommandNotFound       If the main command couldn't be found
     */
    ParseResult parse(NamespaceAccesor accessor, List<String> arguments) throws CommandParseException, CommandNotFound;

    /**
     * Converts the specified line into a {@link List} of Strings using the {@link InputTokenizer} returned by {@link CommandManager#getInputTokenizer()}
     * and executes the {@linkplain CommandManager#execute(NamespaceAccesor, List)} method
     *
     * @param accessor The {@link NamespaceAccesor} used to inject things into the Command parsing/execution phase
     * @param line     A String representing the command line to tokenize
     * @return A boolean indicating if a command was executed or not
     * @throws CommandParseException If the parsing phase of the command fails for any reason
     * @throws CommandException      If the execution phase of the command fails for any reason
     * @see CommandManager#execute(NamespaceAccesor, List)
     */
    boolean execute(NamespaceAccesor accessor, String line) throws CommandParseException, CommandException;

    /**
     * Converts the specified line into a {@link List} of Strings using the {@link InputTokenizer} returned by {@link CommandManager#getInputTokenizer()}
     * and executes the {@linkplain CommandManager#getSuggestions(NamespaceAccesor, List)} method
     *
     * @param accessor The {@link NamespaceAccesor} used to inject objects
     * @param line     A String representing the command line to tokenize
     * @return A {@link List} of {@link String} representing the suggestions for the next argument, empty if the player doesn't has permissions
     * @see CommandManager#getSuggestions(NamespaceAccesor, List)
     */
    List<String> getSuggestions(NamespaceAccesor accessor, String line) throws NoPermissionException;

    /**
     * Converts the specified line into a {@link List} of Strings using the {@link InputTokenizer} returned by {@link CommandManager#getInputTokenizer()}
     * and executes the {@linkplain CommandManager#parse(NamespaceAccesor, List)} method
     *
     * @param accessor  The {@link NamespaceAccesor} used to inject objects
     * @param line     A String representing the command line to tokenize
     * @return A {@link ParseResult} indicating the result of the parse and the command to execute
     * @throws CommandParseException If the parsing of the command fails for any reason
     * @throws CommandNotFound       If the main command couldn't be found
     * @see CommandManager#parse(NamespaceAccesor, List)
     */
    ParseResult parse(NamespaceAccesor accessor, String line) throws CommandParseException, CommandNotFound;
}
