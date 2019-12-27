package me.fixeddev.ebcm.bukkit.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.bukkit.BukkitCommandManager;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.part.CommandPart;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerProvider implements ParameterProvider<Player> {
    public static final String SENDER_MODIFIER = "PLAYER_IS_SENDER";

    @Override
    public Result<Player> transform(List<String> arguments, NamespaceAccesor namespaceAccesor, CommandPart part) {
        CommandSender sender = namespaceAccesor.getObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE);

        if (part.getModifiers().contains(SENDER_MODIFIER)) {
            if (sender == null) {
                return Result.createResult("Failed to get command sender!", new CommandException("Failed to get CommandSender, maybe the namespace wasn't provided with the command sender when executing the command?"));
            }

            if (!(sender instanceof Player)) {
                return Result.createResultOfMessage("Only players can execute this command!");
            }

            return Result.createResult((Player) sender);
        }

        String argument = arguments.get(0);

        Player player = Bukkit.getPlayer(argument);

        if(player == null){
            return Result.createResultOfMessage("The player " + argument + " is not online!");
        }

        return Result.createResult(player);
    }
}
