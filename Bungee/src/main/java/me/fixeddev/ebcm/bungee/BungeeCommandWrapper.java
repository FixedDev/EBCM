package me.fixeddev.ebcm.bungee;

import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.CommandUsageException;
import me.fixeddev.ebcm.exception.NoPermissionException;
import me.fixeddev.ebcm.internal.namespace.Namespace;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UnknownFormatConversionException;

public class BungeeCommandWrapper extends Command implements TabExecutor {

    private CommandManager commandManager;
    private String[] aliases;
    private String permission;

    public BungeeCommandWrapper(me.fixeddev.ebcm.Command command, CommandManager commandManager) {
        super(command.getData().getName());

        this.aliases = command.getData().getAliases().toArray(new String[0]);
        this.permission = command.getPermission();

        this.commandManager = commandManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<String> argumentList = Arrays.asList(args);

        Namespace namespace = new Namespace();
        namespace.setObject(CommandSender.class, BungeeCommandManager.SENDER_NAMESPACE, sender);

        try {
            commandManager.execute(namespace, argumentList);
        } catch (CommandUsageException exception) {
            String[] usageExamples = ChatColor.translateAlternateColorCodes(
                    '&',
                    exception.getMessage()
            ).split("\n");

            for (String usage : usageExamples) {
                sender.sendMessage(TextComponent.fromLegacyText(usage));
            }
        } catch (CommandParseException exception) {
            throw new UnknownFormatConversionException("An internal parse exception occurred while executing the command " + getName());
        } catch (CommandException exception) {
            throw new RuntimeException("An unexpected exception occurred while executing the command " + getName(), exception);
        }
    }

    @Override
    public String[] getAliases() {
        return this.aliases;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        List<String> argumentList = Arrays.asList(strings);

        Namespace namespace = new Namespace();
        namespace.setObject(CommandSender.class, BungeeCommandManager.SENDER_NAMESPACE, commandSender);

        try {
            return commandManager.getSuggestions(namespace, argumentList);
        } catch (NoPermissionException e) {
            commandSender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return Collections.emptyList();
    }
}
