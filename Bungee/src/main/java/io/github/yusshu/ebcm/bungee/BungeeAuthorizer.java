package io.github.yusshu.ebcm.bungee;

import me.fixeddev.ebcm.Authorizer;
import me.fixeddev.ebcm.NamespaceAccesor;
import net.md_5.bungee.api.CommandSender;

public class BungeeAuthorizer implements Authorizer {

    @Override
    public boolean isAuthorized(NamespaceAccesor namespace, String permission) {
        CommandSender sender = namespace.getObject(CommandSender.class, BungeeCommandManager.SENDER_NAMESPACE);
        return sender.hasPermission(permission);
    }

}
