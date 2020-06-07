package me.fixeddev.ebcm.guice;

import com.google.inject.Binder;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.parametric.CommandClass;

import java.util.Optional;

class SimpleCommandBinder implements CommandBinder {

    private Multibinder<Command> commandBinder;
    private Multibinder<CommandClass> commandClassBinder;

    public SimpleCommandBinder(Binder binder) {
        commandBinder = Multibinder.newSetBinder(binder, Command.class);
        commandClassBinder = Multibinder.newSetBinder(binder, CommandClass.class);
    }

    @Override
    public void bind(Command command) {
        commandBinder.addBinding().toInstance(command);
    }

    @Override
    public void bind(Class<? extends CommandClass> clazz, Scope scope) {
        commandClassBinder.addBinding()
                .to(clazz)
                .in(Optional.ofNullable(scope)
                        .orElse(Scopes.NO_SCOPE));
    }
}
