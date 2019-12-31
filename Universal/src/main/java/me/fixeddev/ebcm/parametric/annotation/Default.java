package me.fixeddev.ebcm.parametric.annotation;

import me.fixeddev.ebcm.part.CommandPart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The purpose of this annotation is to define the default value of a parameter
 * in an {@link ACommand}.
 * <p>
 * When you set this annotation to any parameter in an {@link ACommand} you set
 * the default value of that parameter and also you set that parameter {@link CommandPart#isRequired()}
 * value to false
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Default {
    String[] value();
}
