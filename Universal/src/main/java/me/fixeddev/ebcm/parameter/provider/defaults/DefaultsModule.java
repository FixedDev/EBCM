package me.fixeddev.ebcm.parameter.provider.defaults;

import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.parameter.provider.ProvidersModule;

public class DefaultsModule implements ProvidersModule {
    @Override
    public void configure(ParameterProviderRegistry registry) {
        registry.registerParameterProvider(Boolean.class, new BooleanProvider());
        registry.registerParameterProvider(Double.class, new DoubleProvider());
        registry.registerParameterProvider(Integer.class, new IntProvider());
        registry.registerParameterProvider(String.class, new StringProvider());
    }
}
