package me.fixeddev.ebcm.parameter.provider;

import me.fixeddev.ebcm.parameter.provider.defaults.DefaultsModule;

import java.util.HashMap;
import java.util.Map;

class ParameterProviderRegistryImpl implements ParameterProviderRegistry {
    private Map<Class<?>, ParameterProvider<?>> parameterProviders;

    ParameterProviderRegistryImpl() {
        parameterProviders = new HashMap<>();

        installModule(new DefaultsModule());
    }

    @Override
    public <T> void registerParameterProvider(Class<T> clazz, ParameterProvider<T> parameterProvider) {
        if (hasRegisteredProvider(clazz)) {
            throw new IllegalStateException("Failed to register parameter provider for class " + clazz.getName() + ", there's already a registered parameter provider for that class!");
        }
        parameterProviders.put(clazz, parameterProvider);
    }

    @Override
    public <T> boolean hasRegisteredProvider(Class<T> clazz) {
        return parameterProviders.containsKey(clazz);
    }

    @Override
    public Map<Class<?>, ParameterProvider<?>> getRegisteredProviders() {
        return parameterProviders;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParameterProvider<T> getParameterProvider(Class<T> clazz) {
        return (ParameterProvider<T>) parameterProviders.get(clazz);
    }
}
