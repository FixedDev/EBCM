package me.fixeddev.ebcm.bukkit;

import me.fixeddev.ebcm.i18n.DefaultI18n;

public class BukkitDefaultI18n extends DefaultI18n {
    public BukkitDefaultI18n() {
        setMessage("bukkit.invalid.commandsender", "Failed to get the command sender!");
        setMessage("bukkit.only.players", "Only players can execute this command!");
        setMessage("bukkit.player.not.online", "The player %s is not online!");
    }
}
