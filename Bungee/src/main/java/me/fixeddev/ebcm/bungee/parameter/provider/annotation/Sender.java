package me.fixeddev.ebcm.bungee.parameter.provider.annotation;

import me.fixeddev.ebcm.bungee.parameter.provider.ProxiedPlayerSenderProvider;
import me.fixeddev.ebcm.parametric.annotation.ModifierAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ModifierAnnotation(ProxiedPlayerSenderProvider.SENDER_MODIFIER)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Sender {
}
