package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandException;

public interface CommandAction {
    boolean execute(CommandContext parameters) throws CommandException;
}
