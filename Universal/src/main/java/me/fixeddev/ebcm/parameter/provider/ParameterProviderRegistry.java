package me.fixeddev.ebcm.parameter.provider;

import java.util.Map;

public interface ParameterProviderRegistry {
    static ParameterProviderRegistry createRegistry() {
        return new ParameterProviderRegistryImpl();
    }

    Map<Class<?>, ParameterProvider<?>> getRegisteredProviders();

    <T> void registerParameterProvider(Class<T> clazz, ParameterProvider<T> parameterProvider);

    <T> ParameterProvider<T> getParameterProvider(Class<T> clazz);

    <T> boolean hasRegisteredProvider(Class<T> clazz);

    default void installModule(ProvidersModule module) {
        module.configure(this);
    }
}
