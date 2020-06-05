package me.fixeddev.ebcm.parameter.provider.defaults;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.parameter.provider.SingleArgumentProvider;
import me.fixeddev.ebcm.part.CommandPart;

public class IntProvider implements SingleArgumentProvider<Integer> {
    @Override
    public Result<Integer> transform(String argument, NamespaceAccesor namespaceAccesor, CommandPart part) {
        try {
            return Result.createResult(Integer.parseInt(argument));
        } catch (NumberFormatException ex) {
            return Result.createResultOfMessage("%provider.invalid.int%", argument);
        }
    }
}
