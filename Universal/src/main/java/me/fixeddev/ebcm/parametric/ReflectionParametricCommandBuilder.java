package me.fixeddev.ebcm.parametric;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandAction;
import me.fixeddev.ebcm.CommandContext;
import me.fixeddev.ebcm.CommandData;
import me.fixeddev.ebcm.ImmutableCommand;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parametric.annotation.ACommand;
import me.fixeddev.ebcm.parametric.annotation.ConsumedArgs;
import me.fixeddev.ebcm.parametric.annotation.Default;
import me.fixeddev.ebcm.parametric.annotation.Flag;
import me.fixeddev.ebcm.parametric.annotation.Injected;
import me.fixeddev.ebcm.parametric.annotation.ModifierAnnotation;
import me.fixeddev.ebcm.parametric.annotation.Named;
import me.fixeddev.ebcm.parametric.annotation.ParentArg;
import me.fixeddev.ebcm.parametric.annotation.Required;
import me.fixeddev.ebcm.parametric.annotation.SubCommandClasses;
import me.fixeddev.ebcm.parametric.annotation.Usage;
import me.fixeddev.ebcm.part.ArgumentPart;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.part.FlagPart;
import me.fixeddev.ebcm.part.InjectedValuePart;
import me.fixeddev.ebcm.part.SubCommandPart;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            CommandPart part = fromParameter(parameter);

            if (part == null) {
                continue;
            }

            commandBuilder.addPart(part);
        }

        commandBuilder.setAction(actionOfMethod(commandClass, method));

        Usage usage = method.getAnnotation(Usage.class);
        if (usage != null) {
            if (!usage.usage()[0].equals("_!!_NOT_OVERRIDE_!!_")) {
                commandBuilder.setUsage(String.join("\n", usage.usage()));
            }
        }


        return commandBuilder.build();
    }

    @Override
    public List<Command> fromClass(CommandClass commandClass) {
        Class<?> clazz = commandClass.getClass();

        ACommand commandAnnotation = clazz.getAnnotation(ACommand.class);

        Map<String, Command> commands = new HashMap<>();
        List<Command> commandList = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (method.getReturnType() != boolean.class && method.getReturnType() != Boolean.class) {
                continue;
            }

            if (!method.isAnnotationPresent(ACommand.class)) {
                continue;
            }

            Command command = fromMethod(commandClass, method);

            commands.put(command.getData().getName(), command);
            commandList.add(command);
        }

        if (commandAnnotation != null) {
            Command command = commands.get("");

            String[] names = commandAnnotation.names();

            CommandData.Builder dataBuilder = CommandData.builder(commandAnnotation.names()[0])
                    .setAliases(Arrays.asList(Arrays.copyOfRange(names, 1, names.length)))
                    .setDescription(commandAnnotation.desc());

            ImmutableCommand.Builder builder = ImmutableCommand.builder(dataBuilder);

            builder.setPermission(commandAnnotation.permission())
                    .setPermissionMessage(commandAnnotation.permissionMessage());

            if (command != null) {
                commands.remove("");
                commandList.remove(command);

                builder.setUsage(command.getUsage())
                        .setAction(command.getAction())
                        .setCommandParts(command.getParts());
            }

            SubCommandPart.Builder subCommandBuilder = SubCommandPart.builder("subcommand");

            if (clazz.isAnnotationPresent(Required.class)) {
                subCommandBuilder.setRequired(true);
            }

            SubCommandClasses classesAnnotation = clazz.getAnnotation(SubCommandClasses.class);

            if (classesAnnotation != null) {
                for (Class<? extends CommandClass> subCommandClass : classesAnnotation.value()) {
                    if (subCommandClass == clazz) {
                        continue;
                    }

                    try {
                        List<Command> subCommands = fromClass(createSubCommandInstance(subCommandClass, clazz, commandClass));
                        commandList.addAll(subCommands);
                    } catch (RuntimeException e) {
                        continue;
                    }
                }
            }

            Usage usage = clazz.getAnnotation(Usage.class);
            if (usage != null) {
                if (!usage.usage()[0].equals("_!!_NOT_OVERRIDE_!!_")) {
                    builder.setUsage(String.join("\n", usage.usage()));
                }
            }

            builder.addPart(subCommandBuilder
                    .setCommands(commandList)
                    .build());

            return Collections.singletonList(builder.build());
        }

        return commandList;
    }

    private CommandClass createSubCommandInstance(Class<?> clazz, Class<?> upperCommandClass, CommandClass upperCommand) {
        try {
            Constructor constructor;
            boolean useUpperClass = true;
            try {
                constructor = clazz.getConstructor(upperCommandClass);
            } catch (NoSuchMethodException e) {
                constructor = clazz.getConstructor();
                useUpperClass = false;
            }

            boolean accessible = constructor.isAccessible();

            constructor.setAccessible(true);

            CommandClass instance;

            if (useUpperClass) {
                instance = (CommandClass) constructor.newInstance(upperCommand);
            } else {
                instance = (CommandClass) constructor.newInstance();
            }
            constructor.setAccessible(accessible);

            return instance;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private CommandPart fromParameter(Parameter parameter) {
        if (parameter.isAnnotationPresent(ParentArg.class)) {
            return null;
        }

        Class<?> type = parameter.getType();
        String name = getName(parameter);
        int consumedArgs = getConsumedArgs(parameter);
        Optional<String[]> defaultValues = getDefault(parameter);

        Flag flag = parameter.getAnnotation(Flag.class);
        Injected injected = parameter.getAnnotation(Injected.class);

        List<String> modifiers = new ArrayList<>();

        for (Annotation annotation : parameter.getAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();

            ModifierAnnotation modifierAnnotation = annotationType.getAnnotation(ModifierAnnotation.class);

            // This is not a modifier annotation, ignore it
            if (modifierAnnotation == null) {
                continue;
            }

            modifiers.add(modifierAnnotation.value());
        }

        if (injected != null && flag != null) {
            throw new IllegalArgumentException("The provided parameter has a Flag annotation and a Injected annotation, it should have only one of the two!");
        }

        if (flag != null) {
            if (type != boolean.class && type != Boolean.class) {
                throw new IllegalArgumentException("The provided parameter has a Flag annotation but it doesn't a boolean!");
            }

            return FlagPart.builder(name, flag.value())
                    .setAllModifiers(modifiers)
                    .build();
        }

        if (injected != null) {
            return InjectedValuePart.builder(name, type)
                    .setRequired(injected.value())
                    .setAllModifiers(modifiers)
                    .build();
        }

        return ArgumentPart.builder(name, type)
                .setConsumedArguments(consumedArgs)
                .setRequired(!defaultValues.isPresent())
                .setDefaultValues(Arrays.asList(defaultValues.orElse(new String[0])))
                .setAllModifiers(modifiers)
                .build();
    }

    private CommandAction actionOfMethod(CommandClass commandClass, Method method) {
        class ParametricCommandAction implements CommandAction {

            List<CommandPart> commandParts = new ArrayList<>();

            @Override
            public boolean execute(CommandContext parameters) throws CommandException {
                if (commandParts.isEmpty()) {
                    computeParts(parameters);
                }

                List<Object> params = new ArrayList<>();

                for (CommandPart part : commandParts) {
                    if (part instanceof FlagPart || part instanceof ArgumentPart || part instanceof InjectedValuePart) {
                        if (parameters.hasValue(part)) {
                            params.add(parameters.getRawValue(part));
                        } else {
                            if (part.isRequired()) {
                                throw new CommandException("The value for the required part" + part.getName() + " is missing!");
                            }
                            params.add(null);
                        }
                    }
                }

                boolean accessible = method.isAccessible();

                try {
                    method.setAccessible(true);

                    boolean result = (boolean) method.invoke(commandClass, params.toArray());
                    method.setAccessible(accessible);

                    return result;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new CommandException("An exception occurred while executing the command", e);
                }
            }

            private void computeParts(CommandContext parameters) {
                for (Parameter parameter : method.getParameters()) {
                    String name = getName(parameter);
                    int indexOf = 0;

                    ParentArg parentArg = parameter.getAnnotation(ParentArg.class);
                    if (parentArg != null) {
                        indexOf = parentArg.value();
                    }

                    commandParts.add(parameters.getParts(name).get(indexOf));
                }
            }

        }

        return new ParametricCommandAction();
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
