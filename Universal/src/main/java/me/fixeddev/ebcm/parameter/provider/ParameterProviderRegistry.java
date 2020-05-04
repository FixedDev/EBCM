package me.fixeddev.ebcm.parameter.provider;

import java.util.Map;

public interface ParameterProviderRegistry {
    static ParameterProviderRegistry createRegistry() {
        return new ParameterProviderRegistryImpl();
    }

    Map<Key<?>, ParameterProvider<?>> getRegisteredProviders();

    <T> void registerParameterProvider(Key<T> key, ParameterProvider<T> parameterProvider);

    default <T> void registerParameterProvider(Class<T> clazz, ParameterProvider<T> parameterProvider) {
        registerParameterProvider(new Key<>(clazz), parameterProvider);
    }

    default <T> ParameterProvider<T> getParameterProvider(Class<T> clazz) {
        return getParameterProvider(new Key<>(clazz));
    }

    <T> ParameterProvider<T> getParameterProvider(Key<T> key);

    default <T> boolean hasRegisteredProvider(Class<T> clazz){
        return hasRegisteredProvider(new Key<>(clazz));
    }

    <T> boolean hasRegisteredProvider(Key<T> key);

    default void installModule(ProvidersModule module) {
        module.configure(this);
    }
}
