package me.fixeddev.ebcm;

import me.fixeddev.ebcm.i18n.Message;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.part.SubCommandPart;
import me.fixeddev.ebcm.util.UsageBuilder;

import java.util.List;

public class DefaultCommandUsageHandler implements CommandUsageHandler {
    @Override
    public boolean handleMissing(ParsingContext context, CommandPart part) {
        String message = context.getCommandManager().getI18n().getMessage(Message.MISSING_ARGUMENT, context.getExecutionPath(), context.getNamespace());

        if (message == null) {
            message = "Missing arguments for required part %s minimum arguments required: %s";
        }

        context.getCommandManager().getMessenger().sendMessage(context.getNamespace(), message, part.getName(), context.getParser().getNeededArguments() + "");
        context.getCommandManager().getMessenger().sendMessage(context.getNamespace(), UsageBuilder.getUsageForCommand(null, context.getCurrentCommand(), context.getParser().getCommandLabel()));

        return false;
    }

    @Override
    public boolean handleInvalid(ParsingContext context, CommandPart part, List<String> arguments) {
        if(part instanceof SubCommandPart) {
            String message = context.getCommandManager().getI18n().getMessage(Message.INVALID_SUBCOMMAND, context.getExecutionPath(), context.getNamespace());

            if (message == null) {
                message = "Invalid sub-command, valid values: %s";
            }

            context.getCommandManager().getMessenger().sendMessage(context.getNamespace(), message, ((SubCommandPart) part).getCommandMappings().keySet());

            context.getCommandManager().getMessenger().sendMessage(context.getNamespace(), UsageBuilder.getUsageForCommand(null, context.getCurrentCommand(), context.getParser().getCommandLabel()));
        }
        return false;
    }
}
