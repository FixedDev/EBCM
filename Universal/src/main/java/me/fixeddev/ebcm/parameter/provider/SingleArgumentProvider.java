package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;

import java.util.List;

public interface SingleArgumentProvider<T> extends ParameterProvider<T> {
    @Override
    default Result<T> transform(List<String> arguments, NamespaceAccesor namespaceAccesor){
        if(arguments.size() != 1){
            return Result.createResult("Internal error!",
                    new IllegalArgumentException("A single argument provider should receive only one argument!"));
        }

        return transform(arguments.get(0), namespaceAccesor);
    }

    Result<T> transform(String arguments, NamespaceAccesor namespaceAccesor);
}
