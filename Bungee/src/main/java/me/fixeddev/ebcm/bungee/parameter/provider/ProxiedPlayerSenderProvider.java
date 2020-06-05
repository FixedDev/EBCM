package me.fixeddev.ebcm.bungee.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.bungee.BungeeCommandManager;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parameter.provider.InjectedProvider;
import me.fixeddev.ebcm.part.CommandPart;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class ProxiedPlayerSenderProvider implements InjectedProvider<ProxiedPlayer> {

    public static final String SENDER_MODIFIER = "PLAYER_IS_SENDER";

    @Override
    public Result<ProxiedPlayer> transform(NamespaceAccesor namespaceAccesor, CommandPart part) {
        CommandSender sender = namespaceAccesor.getObject(CommandSender.class, BungeeCommandManager.SENDER_NAMESPACE);

        if(part.getModifiers().contains(SENDER_MODIFIER)) {
            if(sender == null) {
                return Result.createResult(
                        "%bungee.invalid.commandsender%",
                        new CommandException("Failed to get CommandSender, maybe the namespace wasn't " +
                                "provided with the command sender when executing the command?")
                );
            }

            if(!(sender instanceof ProxiedPlayer)) {
                return Result.createResultOfMessage("%bungee.only.players%");
            }

            return Result.createResult((ProxiedPlayer) sender);
        }

        return Result.createResultOfMessage("Internal error.");
    }

    @Override
    public List<String> getSuggestions(String startsWith) {
        List<String> suggestions = new ArrayList<>();

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if(player.getName().startsWith(startsWith)){
                suggestions.add(player.getName());
            }
        }

        return suggestions;
    }
}
