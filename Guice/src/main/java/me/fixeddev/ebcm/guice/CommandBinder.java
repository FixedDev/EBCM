package me.fixeddev.ebcm.guice;

import com.google.inject.Scope;
import com.google.inject.Scopes;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.parametric.CommandClass;

public interface CommandBinder {
    void bind(Command command);

    default void bind(Class<? extends CommandClass> clazz) {
        bind(clazz, Scopes.NO_SCOPE);
    }
    void bind(Class<? extends CommandClass> clazz, Scope scope);
}
