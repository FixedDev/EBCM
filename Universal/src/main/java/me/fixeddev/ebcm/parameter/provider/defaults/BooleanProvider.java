package me.fixeddev.ebcm.parameter.provider.defaults;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.parameter.provider.SingleArgumentProvider;

import java.util.Arrays;
import java.util.List;

public class BooleanProvider implements SingleArgumentProvider<Boolean> {
    private static List<String> suggestions = Arrays.asList("true", "false");

    @Override
    public Result<Boolean> transform(String argument, NamespaceAccesor namespaceAccesor) {
        if (!argument.equalsIgnoreCase("true") && !argument.equalsIgnoreCase("false")) {
            return Result.createResultOfMessage("The provided argument(" + argument + ") is not a valid boolean!");
        }

        return Result.createResult(Boolean.parseBoolean(argument));
    }

    @Override
    public List<String> getSuggestions(String startsWith) {
        return suggestions;
    }
}
