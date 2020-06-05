package me.fixeddev.ebcm.bukkit.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.bukkit.BukkitCommandManager;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parameter.provider.InjectedProvider;
import me.fixeddev.ebcm.part.CommandPart;
import org.bukkit.command.CommandSender;

public class CommandSenderProvider implements InjectedProvider<CommandSender> {

    @Override
    public Result<CommandSender> transform(NamespaceAccesor namespaceAccesor, CommandPart part) {
        CommandSender sender = namespaceAccesor.getObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE);

        if (sender == null) {
            return Result.createResult("%bukkit.invalid.commandsender%", new CommandException("Failed to get CommandSender, maybe the namespace wasn't provided with the command sender when executing the command?"));
        }

        return Result.createResult(sender);
    }
}
