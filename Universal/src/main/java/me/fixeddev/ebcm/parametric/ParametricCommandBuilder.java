package me.fixeddev.ebcm.parametric;

import me.fixeddev.ebcm.Command;

import java.lang.reflect.Method;
import java.util.List;

public interface ParametricCommandBuilder {
    Command fromMethod(CommandClass commandClass, Method method);

    List<Command> fromClass(CommandClass commandClass);

}
