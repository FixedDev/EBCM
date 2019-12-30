package me.fixeddev.ebcm.parametric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ACommand {

    String[] names();

    String desc() default "";

    String permission() default "";

    String permissionMessage() default "No permission.";

}
