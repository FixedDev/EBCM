package io.github.yusshu.ebcm.bungee.parameter.provider;

import io.github.yusshu.ebcm.bungee.BungeeCommandManager;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.part.CommandPart;
import net.md_5.bungee.api.CommandSender;

import java.util.List;

public class CommandSenderProvider implements ParameterProvider<CommandSender> {

    @Override
    public Result<CommandSender> transform(List<String> arguments, NamespaceAccesor namespaceAccesor, CommandPart part) {
        CommandSender sender = namespaceAccesor.getObject(CommandSender.class, BungeeCommandManager.SENDER_NAMESPACE);

        if(sender == null) {
            return Result.createResult(
                    "Failed to get command sender!",
                    new CommandException("Failed to get CommandSender, maybe the namespace wasn't " +
                            "provided with the command sender when executing the command?")
            );
        }

        return Result.createResult(sender);
    }

}
