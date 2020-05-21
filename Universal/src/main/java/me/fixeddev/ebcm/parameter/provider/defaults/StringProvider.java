package me.fixeddev.ebcm.parameter.provider.defaults;

import me.fixeddev.ebcm.NamespaceAccesor;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.parameter.provider.ParameterProvider;
import me.fixeddev.ebcm.part.CommandPart;
import me.fixeddev.ebcm.stack.StackSlice;

import java.util.StringJoiner;

/*
Ok, if you're reading this, you may ask why this class is not a SingleArgumentProvider
Well, that's because a string can be a joined string, that means that a list of string arguments
can be joined to form a single string in this form "arg arg2 arg3"
 */
public class StringProvider implements ParameterProvider<String> {

    @Override
    public Result<String> transform(StackSlice arguments, NamespaceAccesor namespaceAccesor, CommandPart part) throws NoMoreArgumentsException {
        StringJoiner joiner = new StringJoiner(" ");

        while (arguments.hasNext()){
            joiner.add(arguments.next());
        }

        return Result.createResult(joiner.toString());
    }
}
