package me.fixeddev.ebcm.bungee;

import me.fixeddev.ebcm.Messager;
import me.fixeddev.ebcm.NamespaceAccesor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeMessager implements Messager {
    @Override
    public void sendMessage(NamespaceAccesor namespace, String messageId, String message) {
        CommandSender sender = namespace.getObject(CommandSender.class, BungeeCommandManager.SENDER_NAMESPACE);

        message = ChatColor.translateAlternateColorCodes('&',message);
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }
}
