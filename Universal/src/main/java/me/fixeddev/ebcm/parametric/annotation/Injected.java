package me.fixeddev.ebcm.parametric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Injected {
    /**
     * @return a boolean value that indicates if this injected value is required or not
     */
    boolean value() default false;
}
