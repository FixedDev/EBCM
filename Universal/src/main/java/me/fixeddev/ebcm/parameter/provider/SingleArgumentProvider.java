package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.stack.StackSlice;

public interface SingleArgumentProvider<T> extends ParameterProvider<T> {
    @Override
    default Result<T> transform(StackSlice arguments, NamespaceAccesor namespaceAccesor, CommandPart part) throws NoMoreArgumentsException {
        if (arguments.getArgumentsLeft() != 1) {
            return Result.createResult("Internal error!",
                   new IllegalArgumentException("A single argument provider should receive only one argument!"));
        }

        return transform(arguments.next(), namespaceAccesor, part);
    }

    Result<T> transform(String arguments, NamespaceAccesor namespaceAccesor, CommandPart part);
}
