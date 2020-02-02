package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.part.CommandPart;

import java.util.List;

public interface InjectedProvider<T>  extends ParameterProvider<T> {
    @Override
    default Result<T> transform(List<String> arguments, NamespaceAccesor namespaceAccesor, CommandPart part) {
        if(!arguments.isEmpty()){
            throw new IllegalStateException("An injected provider shouldn't have any arguments!");
        }

        return transform(namespaceAccesor, part);
    }

    @Override
    default boolean isInjected() {
        return true;
    }

    Result<T> transform(NamespaceAccesor namespaceAccesor, CommandPart part);

}
