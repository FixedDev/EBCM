package me.fixeddev.ebcm;

import me.fixeddev.ebcm.internal.CommandLineParser;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.stack.ArgumentStack;

import java.util.List;

public interface ParsingContext {
    CommandLineParser getParser();

    ArgumentStack getStack();

    NamespaceAccesor getNamespace();

    List<Command> getExecutionPath();

    Command getCurrentCommand();

    ParameterProviderRegistry getRegistry();

    CommandManager getCommandManager();
}
