package me.fixeddev.ebcm;

public interface CommandAction {
    boolean execute(CommandContext parameters);
}
