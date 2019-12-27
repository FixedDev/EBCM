package me.fixeddev.ebcm.bukkit.parameter.provider;

import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.parameter.provider.ProvidersModule;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitModule implements ProvidersModule {
    @Override
    public void configure(ParameterProviderRegistry registry) {
        //registry.registerParameterProvider(CommandSender.class, new CommandSenderProvider());
        registry.registerParameterProvider(OfflinePlayer.class, new OfflinePlayerProvider());
        registry.registerParameterProvider(Player.class, new PlayerProvider());
    }
}
