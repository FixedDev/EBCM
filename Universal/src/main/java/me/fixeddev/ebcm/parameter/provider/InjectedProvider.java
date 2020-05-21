package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.stack.NoOpStackSlice;
import me.fixeddev.ebcm.stack.StackSlice;


public interface InjectedProvider<T> extends ParameterProvider<T> {
    @Override
    default Result<T> transform(StackSlice slice, NamespaceAccesor namespaceAccesor, CommandPart part) {
        if (!(slice instanceof NoOpStackSlice)) {
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
