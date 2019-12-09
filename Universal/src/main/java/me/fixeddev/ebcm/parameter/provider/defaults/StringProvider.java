package me.fixeddev.ebcm.parameter.provider.defaults;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;

import java.util.List;

/*
Ok, if you're reading this, you may ask why this class is not a SingleArgumentProvider
Well, that's because a string can be a joined string, that means that a list of string arguments
can be joined to form a single string in this form "arg arg2 arg3"
 */
public class StringProvider implements ParameterProvider<String> {

    @Override
    public Result<String> transform(List<String> arguments, NamespaceAccesor namespaceAccesor) {
        return Result.createResult(String.join(" ", arguments));
    }
}
