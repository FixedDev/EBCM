package me.fixeddev.ebcm.bukkit.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.stack.StackSlice;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerProvider implements ParameterProvider<Player> {

    @Override
    public Result<Player> transform(StackSlice arguments, NamespaceAccesor namespaceAccesor, CommandPart part) throws NoMoreArgumentsException {
        String argument = arguments.next();

        Player player = Bukkit.getPlayer(argument);

        if (player == null) {
            return Result.createResultOfMessage("%bukkit.player.not.online%", argument);
        }

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
