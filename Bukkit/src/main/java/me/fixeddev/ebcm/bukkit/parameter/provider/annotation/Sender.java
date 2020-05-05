package me.fixeddev.ebcm.bukkit.parameter.provider.annotation;

import me.fixeddev.ebcm.bukkit.parameter.provider.PlayerSenderProvider;
import me.fixeddev.ebcm.parametric.annotation.ModifierAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ModifierAnnotation(PlayerSenderProvider.SENDER_MODIFIER)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Sender {
}
