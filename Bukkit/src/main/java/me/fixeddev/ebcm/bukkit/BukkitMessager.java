package me.fixeddev.ebcm.bukkit;

import me.fixeddev.ebcm.Messager;
import me.fixeddev.ebcm.NamespaceAccesor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BukkitMessager implements Messager {
    @Override
    public void sendMessage(NamespaceAccesor namespace, String messageId, String message, String... parameters) {
        CommandSender sender = namespace.getObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE);

        message = String.format(message, (Object[]) parameters);

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
