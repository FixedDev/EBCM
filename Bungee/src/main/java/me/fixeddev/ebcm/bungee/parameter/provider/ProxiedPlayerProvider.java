package me.fixeddev.ebcm.bungee.parameter.provider;

import me.fixeddev.ebcm.bungee.BungeeCommandManager;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.part.CommandPart;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class ProxiedPlayerProvider implements ParameterProvider<ProxiedPlayer> {

    public static final String SENDER_MODIFIER = "PLAYER_IS_SENDER";

    @Override
    public Result<ProxiedPlayer> transform(List<String> arguments, NamespaceAccesor namespaceAccesor, CommandPart part) {
        CommandSender sender = namespaceAccesor.getObject(CommandSender.class, BungeeCommandManager.SENDER_NAMESPACE);

        if(part.getModifiers().contains(SENDER_MODIFIER)) {
            if(sender == null) {
                return Result.createResult(
                        "Failed to get command sender!",
                        new CommandException("Failed to get CommandSender, maybe the namespace wasn't " +
                                "provided with the command sender when executing the command?")
                );
            }

            if(!(sender instanceof ProxiedPlayer)) {
                return Result.createResultOfMessage("Only players can execute this command!");
            }

            return Result.createResult((ProxiedPlayer) sender);
        }

        String playerName = arguments.get(0);
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);

        if(player == null) {
            return Result.createResultOfMessage("The player " + playerName + " is not online!");
        }

        return Result.createResult(player);
    }

}
