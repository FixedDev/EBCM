package me.fixeddev.ebcm.bukkit;

import me.fixeddev.ebcm.Authorizer;
import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.bukkit.parameter.provider.CommandSenderProvider;
import org.bukkit.command.CommandSender;

public class BukkitAuthorizer implements Authorizer {
    @Override
    public boolean isAuthorized(NamespaceAccesor namespace, String permission) {
        CommandSender sender = namespace.getObject(CommandSender.class, CommandSenderProvider.SENDER_NAMESPACE);

        return sender.hasPermission(permission);
    }
}
