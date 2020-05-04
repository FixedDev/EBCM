package me.fixeddev.ebcm.bungee.parameter.provider;

import me.fixeddev.ebcm.parameter.provider.Key;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.parameter.provider.ProvidersModule;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeModule implements ProvidersModule {

    @Override
    public void configure(ParameterProviderRegistry registry) {
        registry.registerParameterProvider(CommandSender.class, new CommandSenderProvider());
        registry.registerParameterProvider(ProxiedPlayer.class, new ProxiedPlayerProvider());
        registry.registerParameterProvider(new Key<>(ProxiedPlayerSenderProvider.SENDER_MODIFIER, ProxiedPlayer.class), new ProxiedPlayerSenderProvider());
    }

}
