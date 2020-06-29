package me.fixeddev.ebcm.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.OptionalBinder;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.SimpleCommandManager;
import me.fixeddev.ebcm.parametric.ParametricCommandBuilder;
import me.fixeddev.ebcm.parametric.ReflectionParametricCommandBuilder;

public class CommandManagerModule implements Module {

    @Override
    public void configure(Binder binder) {
		binder.bind(CommandBinder.class).to(SimpleCommandBinder.class);
		
        OptionalBinder.newOptionalBinder(binder, CommandManager.class)
                .setDefault()
                .to(SimpleCommandManager.class)
                .in(Scopes.SINGLETON);

        OptionalBinder.newOptionalBinder(binder, ParametricCommandBuilder.class)
                .setDefault()
                .to(ReflectionParametricCommandBuilder.class)
                .in(Scopes.SINGLETON);
    }
}
