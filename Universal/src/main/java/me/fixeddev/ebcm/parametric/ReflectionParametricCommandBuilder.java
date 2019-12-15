package me.fixeddev.ebcm.parametric;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandAction;
import me.fixeddev.ebcm.CommandData;
import me.fixeddev.ebcm.ImmutableCommand;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parametric.annotation.ACommand;
import me.fixeddev.ebcm.parametric.annotation.ConsumedArgs;
import me.fixeddev.ebcm.parametric.annotation.Default;
import me.fixeddev.ebcm.parametric.annotation.Flag;
import me.fixeddev.ebcm.parametric.annotation.Named;
import me.fixeddev.ebcm.part.ArgumentPart;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.part.FlagPart;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReflectionParametricCommandBuilder implements ParametricCommandBuilder {

    @Override
    public Command fromMethod(CommandClass commandClass, Method method) {
        ACommand commandAnnotation = method.getAnnotation(ACommand.class);

        if (commandAnnotation == null) {
            throw new IllegalArgumentException("The provided method isn't annotated with an ACommand annotation");
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("The provided method doesn't has public visibility!");
        }

        if (method.getReturnType() != boolean.class && method.getReturnType() != Boolean.class) {
            throw new IllegalArgumentException("The provided method doesn't return a boolean value!");
        }

        String[] names = commandAnnotation.names();

        ImmutableCommand.Builder commandBuilder = ImmutableCommand.builder(
                //
                CommandData.builder(names[0])
                        .setDescription(commandAnnotation.desc())
                        .setAliases(Arrays.asList(Arrays.copyOfRange(names, 1, names.length)))
                //
        ).setPermission(commandAnnotation.permission())
                .setPermissionMessage(commandAnnotation.permissionMessage());

        for (Parameter parameter : method.getParameters()) {
            commandBuilder.addPart(fromParameter(parameter));
        }

        commandBuilder.setAction(actionOfMethod(commandClass, method));

        return commandBuilder.build();
    }

    @Override
    public List<Command> fromClass(CommandClass commandClass) {
        List<Command> commands = new ArrayList<>();
        for (Method method : commandClass.getClass().getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (method.getReturnType() != boolean.class && method.getReturnType() != Boolean.class) {
                continue;
            }

            if(!method.isAnnotationPresent(ACommand.class)){
                continue;
            }

            commands.add(fromMethod(commandClass, method));
        }

        return commands;
    }

    private CommandPart fromParameter(Parameter parameter) {
        Class<?> type = parameter.getType();
        String name = getName(parameter);
        int consumedArgs = getConsumedArgs(parameter);
        Optional<String[]> defaultValues = getDefault(parameter);

        Flag flag = parameter.getAnnotation(Flag.class);

        if (flag != null) {
            return FlagPart.builder(name, flag.value())
                    .build();
        }

        return ArgumentPart.builder(name, type)
                .setConsumedArguments(consumedArgs)
                .setRequired(defaultValues.isPresent())
                .setDefaultValues(Arrays.asList(defaultValues.orElse(new String[0])))
                .build();
    }

    private CommandAction actionOfMethod(CommandClass commandClass, Method method) {
        return (parameters -> {
            List<CommandPart> commandParts = parameters.getCommand().getParts();

            Object[] params = new Object[commandParts.size()];

            int i = 0;
            for (CommandPart part : parameters.getCommand().getParts()) {
                if (part instanceof FlagPart || part instanceof ArgumentPart)
                    params[i++] = part;
            }

            try {
                return (boolean) method.invoke(commandClass, method);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new CommandException("An exception occurred while executing the command", e);
            }
        });
    }

    private String getName(Parameter parameter) {
        Named named = parameter.getAnnotation(Named.class);

        return named != null ? named.value() : parameter.getName();
    }

    private int getConsumedArgs(Parameter parameter) {
        ConsumedArgs consumedArgs = parameter.getAnnotation(ConsumedArgs.class);

        return consumedArgs != null ? consumedArgs.value() : 1;
    }

    private Optional<String[]> getDefault(Parameter parameter) {
        Default defaultA = parameter.getAnnotation(Default.class);

        return defaultA == null ? Optional.empty() : Optional.of(defaultA.value());
    }
}
