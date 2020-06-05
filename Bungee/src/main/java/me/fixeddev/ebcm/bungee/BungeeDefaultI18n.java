package me.fixeddev.ebcm.bungee;

import me.fixeddev.ebcm.i18n.DefaultI18n;

public class BungeeDefaultI18n extends DefaultI18n {
    public BungeeDefaultI18n() {
        setMessage("bungee.invalid.commandsender", "Failed to get the command sender!");
        setMessage("bungee.only.players", "Only players can execute this command!");
        setMessage("bungee.player.not.online", "The player %s is not online!");
    }
}
