package me.fixeddev.ebcm.bungee.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.stack.StackSlice;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class ProxiedPlayerProvider implements ParameterProvider<ProxiedPlayer> {

    @Override
    public Result<ProxiedPlayer> transform(StackSlice slice, NamespaceAccesor namespaceAccesor, CommandPart part) throws NoMoreArgumentsException {
        String playerName = slice.next();
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);

        if (player == null) {
            return Result.createResultOfMessage("%bungee.player.not.online%", playerName);
        }

        return Result.createResult(player);
    }

    @Override
    public List<String> getSuggestions(String startsWith) {
        List<String> suggestions = new ArrayList<>();

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.getName().startsWith(startsWith)) {
                suggestions.add(player.getName());
            }
        }

        return suggestions;
    }
}
