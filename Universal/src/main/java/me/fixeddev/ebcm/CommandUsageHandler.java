package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.CommandUsageException;
import me.fixeddev.ebcm.part.CommandPart;

import java.util.List;

public interface CommandUsageHandler {
    boolean handleMissing(ParsingContext context, CommandPart part) throws CommandParseException;

    boolean handleInvalid(ParsingContext context, CommandPart part, List<String> arguments) throws CommandParseException;

    void handleExecution(CommandContext commandContext, ParseResult result, CommandManager manager) throws CommandUsageException;
}
