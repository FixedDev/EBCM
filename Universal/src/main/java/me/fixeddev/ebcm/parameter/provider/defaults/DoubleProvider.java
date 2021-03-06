package me.fixeddev.ebcm.parameter.provider.defaults;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.parameter.provider.SingleArgumentProvider;
import me.fixeddev.ebcm.part.CommandPart;

public class DoubleProvider implements SingleArgumentProvider<Double> {
    @Override
    public Result<Double> transform(String argument, NamespaceAccesor namespaceAccesor, CommandPart part) {
        try {
            return Result.createResult(Double.parseDouble(argument));
        } catch (NumberFormatException ex) {
            return Result.createResultOfMessage("The argument " + argument + " is not a valid number!");
        }
    }
}
