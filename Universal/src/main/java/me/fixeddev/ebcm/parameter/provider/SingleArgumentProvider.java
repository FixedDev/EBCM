package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.part.CommandPart;

import java.util.List;

public interface SingleArgumentProvider<T> extends ParameterProvider<T> {
    @Override
    default Result<T> transform(List<String> arguments, NamespaceAccesor namespaceAccesor, CommandPart part) {
        if (arguments.size() != 1) {
            return Result.createResult("Internal error!",
                    new IllegalArgumentException("A single argument provider should receive only one argument!"));
        }

        return transform(arguments.get(0), namespaceAccesor, part);
    }

    Result<T> transform(String arguments, NamespaceAccesor namespaceAccesor, CommandPart part);
}
