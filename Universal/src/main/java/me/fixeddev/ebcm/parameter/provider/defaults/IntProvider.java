package me.fixeddev.ebcm.parameter.provider.defaults;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.parameter.provider.SingleArgumentProvider;

public class IntProvider implements SingleArgumentProvider<Integer> {
    @Override
    public Result<Integer> transform(String argument, NamespaceAccesor namespaceAccesor) {
        try {
            return Result.createResult(Integer.parseInt(argument));
        } catch (NumberFormatException ex) {
            return Result.createResultOfMessage("The argument " + argument + " is not a valid number!");
        }
    }
}
