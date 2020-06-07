package me.fixeddev.ebcm.guice;

import com.google.inject.Inject;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.parametric.CommandClass;
import me.fixeddev.ebcm.parametric.ParametricCommandBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimpleCommandBindingManager implements CommandBindingManager {

    private Set<Command> commandSet;
    private Set<CommandClass> commandClassSet;

    @Inject
    private CommandManager commandManager;

    @Inject
    private ParametricCommandBuilder commandBuilder;

    @Inject
    public SimpleCommandBindingManager(Set<Command> commandSet, Set<CommandClass> commandClassSet) {
        this.commandSet = commandSet;
        this.commandClassSet = commandClassSet;
    }

    @Override
    public void registerCommands() {
        List<Command> commandsToRegister = new ArrayList<>(commandSet);

        for (CommandClass object : commandClassSet) {
            commandsToRegister.addAll(commandBuilder.fromClass(object));
        }

        commandManager.registerCommands(commandsToRegister);
    }
}
