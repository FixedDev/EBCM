package me.fixeddev.ebcm.bukkit;

import me.fixeddev.ebcm.Authorizer;
import me.fixeddev.ebcm.NamespaceAccesor;
import org.bukkit.command.CommandSender;

public class BukkitAuthorizer implements Authorizer {
    @Override
    public boolean isAuthorized(NamespaceAccesor namespace, String permission) {
        if(permission.isEmpty()){
            return true;
        }

        CommandSender sender = namespace.getObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE);

        return sender.hasPermission(permission);
    }
}
