package me.fixeddev.ebcm.bungee;

import me.fixeddev.ebcm.Messenger;
import me.fixeddev.ebcm.NamespaceAccesor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeMessenger implements Messenger {
    @Override
    public void sendMessage(NamespaceAccesor namespace, String message, Object... parameters) {
        CommandSender sender = namespace.getObject(CommandSender.class, BungeeCommandManager.SENDER_NAMESPACE);

        message = String.format(message, parameters);

        message = ChatColor.translateAlternateColorCodes('&', message);
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }
}
