package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.parameter.provider.defaults.DefaultsModule;

import java.util.HashMap;
import java.util.Map;

class ParameterProviderRegistryImpl implements ParameterProviderRegistry {
    private Map<Key<?>, ParameterProvider<?>> parameterProviders;

    ParameterProviderRegistryImpl() {
        parameterProviders = new HashMap<>();

        installModule(new DefaultsModule());
    }

    @Override
    public <T> boolean hasRegisteredProvider(Key<T> key) {
        return parameterProviders.containsKey(key);
    }

    @Override
    public Map<Key<?>, ParameterProvider<?>> getRegisteredProviders() {
        return parameterProviders;
    }

    @Override
    public <T> void registerParameterProvider(Key<T> key, ParameterProvider<T> parameterProvider) {
        if (hasRegisteredProvider(key)) {
            throw new IllegalStateException("Failed to register parameter provider for key " + key.toString() + ", there's already a registered parameter provider for that class!");
        }

        parameterProviders.put(key, parameterProvider);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParameterProvider<T> getParameterProvider(Key<T> key) {
        return (ParameterProvider<T>) parameterProviders.get(key);
    }
}
