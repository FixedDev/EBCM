package me.fixeddev.ebcm.bukkit.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.bukkit.BukkitCommandManager;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.part.CommandPart;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class OfflinePlayerProvider implements ParameterProvider<OfflinePlayer> {
    @Override
    public Result<OfflinePlayer> transform(List<String> arguments, NamespaceAccesor namespaceAccesor, CommandPart part) {
        CommandSender sender = namespaceAccesor.getObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE);

        if (part.getModifiers().contains(PlayerSenderProvider.SENDER_MODIFIER)) {
            if (sender == null) {
                return Result.createResult("Failed to get command sender!", new CommandException("Failed to get CommandSender, maybe the namespace wasn't provided with the command sender when executing the command?"));
            }

            if (!(sender instanceof Player)) {
                return Result.createResultOfMessage("Only players can execute this command!");
            }

            return Result.createResult((Player) sender);
        }

        String argument = arguments.get(0);

        OfflinePlayer player = Bukkit.getOfflinePlayer(argument);

        return Result.createResult(player);
    }

    @Override
    public List<String> getSuggestions(String startsWith) {
        List<String> suggestions = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getName().startsWith(startsWith)){
                suggestions.add(player.getName());
            }
        }

        return suggestions;
    }
}
