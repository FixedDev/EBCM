package me.fixeddev.ebcm.bukkit.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.bukkit.BukkitCommandManager;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parameter.provider.InjectedProvider;
import me.fixeddev.ebcm.part.CommandPart;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerSenderProvider implements InjectedProvider<Player> {
    public static final String SENDER_MODIFIER = "PLAYER_IS_SENDER";

    @Override
    public Result<Player> transform(NamespaceAccesor namespaceAccesor, CommandPart part) {
        CommandSender sender = namespaceAccesor.getObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE);

        if (part.getModifiers().contains(SENDER_MODIFIER)) {
            if (sender == null) {
                return Result.createResult("%bukkit.invalid.commandsender%", new CommandException("Failed to get CommandSender, maybe the namespace wasn't provided with the command sender when executing the command?"));
            }

            if (!(sender instanceof Player)) {
                return Result.createResultOfMessage("%bukkit.only.players%");
            }

            return Result.createResult((Player) sender);
        }

        return Result.createResultOfMessage("Internal error.");
    }
}
