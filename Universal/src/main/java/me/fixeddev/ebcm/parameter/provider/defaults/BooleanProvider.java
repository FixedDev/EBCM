package me.fixeddev.ebcm.parameter.provider.defaults;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.parameter.provider.SingleArgumentProvider;
import me.fixeddev.ebcm.part.CommandPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BooleanProvider implements SingleArgumentProvider<Boolean> {
    private static List<String> suggestions = Arrays.asList("true", "false");

    @Override
    public Result<Boolean> transform(String argument, NamespaceAccesor namespaceAccesor, CommandPart part) {
        if (!argument.equalsIgnoreCase("true") && !argument.equalsIgnoreCase("false")) {
            return Result.createResultOfMessage("%provider.invalid.boolean%", argument);
        }

        return Result.createResult(Boolean.parseBoolean(argument));
    }

    @Override
    public List<String> getSuggestions(String startsWith) {
        List<String> newSuggestions = new ArrayList<>();

        for (String suggestion : suggestions) {
            if(suggestion.startsWith(startsWith)){
                newSuggestions.add(suggestion);
            }
        }

        return newSuggestions;
    }
}
