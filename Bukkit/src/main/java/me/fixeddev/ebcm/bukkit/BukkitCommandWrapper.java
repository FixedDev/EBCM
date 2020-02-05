package me.fixeddev.ebcm.bukkit;

import me.fixeddev.ebcm.Authorizer;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.CommandUsageException;
import me.fixeddev.ebcm.internal.namespace.Namespace;
import me.fixeddev.ebcm.util.UsageBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BukkitCommandWrapper extends Command implements TabCompleter {
    private CommandManager commandManager;

    public BukkitCommandWrapper(me.fixeddev.ebcm.Command command, CommandManager dispatcher) {
        super(command.getData().getName());

        this.setAliases(command.getData().getAliases());

        this.setDescription(command.getData().getDescription());
        this.setUsage(UsageBuilder.getUsageForCommand(null, command, "<command>"));

        this.setPermission(command.getPermission());
        this.setPermissionMessage(command.getPermissionMessage());

        this.commandManager = dispatcher;
    }


    @Override
    public boolean execute(CommandSender commandSender, String label, String[] args) {
        List<String> argumentLine = new ArrayList<>();

        argumentLine.add(label);
        argumentLine.addAll(Arrays.asList(args));

        Namespace namespace = new Namespace();
        namespace.setObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE, commandSender);

        try {
            if (commandManager.execute(namespace, argumentLine)) {
                return true;
            }
        } catch (CommandUsageException ex) {
            String[] usage = ChatColor.translateAlternateColorCodes('&', ex.getMessage())
                    .split("\n");

            for (String s : usage) {
                commandSender.sendMessage(s);
            }
        } catch (CommandParseException e) {
            throw new org.bukkit.command.CommandException("An internal parse exception occurred while executing the command " + label, e);
        } catch (CommandException e) {
            throw new org.bukkit.command.CommandException("An unexpected exception occurred while executing the command " + label, e);
        }

        return false;
    }

    @Override
    // Ignored value, the command manager authorizer manages this
    public boolean testPermissionSilent(CommandSender target) {
        Authorizer authorizer = commandManager.getAuthorizer();

        Namespace namespace = new Namespace();
        namespace.setObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE, target);

        return authorizer.isAuthorized(namespace, getPermission());
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> argumentLine = new ArrayList<>();

        argumentLine.add(s);
        argumentLine.addAll(Arrays.asList(strings));

        Namespace namespace = new Namespace();
        namespace.setObject(CommandSender.class, BukkitCommandManager.SENDER_NAMESPACE, commandSender);

        return commandManager.getSuggestions(namespace, argumentLine);
    }
}
