package me.fixeddev.ebcm.parametric.annotation;

import me.fixeddev.ebcm.parametric.CommandClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation has the purpose to allow multiple layers of subcommands
 * on parametric commands. This annotation will register the specified command classes
 * as subcommands for the annotated command class
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubCommandClasses {
    Class<? extends CommandClass>[] value();
}
