package me.fixeddev.ebcm;

import me.fixeddev.ebcm.internal.CommandLineParser;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.stack.ArgumentStack;

public interface ParsingContext {
    CommandLineParser getParser();

    ArgumentStack getStack();

    NamespaceAccesor getNamespace();

    Command getCurrentCommand();

    ParameterProviderRegistry getRegistry();

    CommandManager getCommandManager();
}
