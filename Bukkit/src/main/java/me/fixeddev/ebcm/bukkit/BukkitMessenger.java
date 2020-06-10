package me.fixeddev.ebcm.bukkit;

import me.fixeddev.ebcm.Messenger;
import me.fixeddev.ebcm.NamespaceAccesor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BukkitMessenger implements Messenger {
    @Override
    public void sendMessage(NamespaceAccesor namespace, String message, Object... parameters) {
        CommandSender sender = namespace.getObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE);

        String newMessage = String.format(message, parameters);

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', newMessage));
    }
}
