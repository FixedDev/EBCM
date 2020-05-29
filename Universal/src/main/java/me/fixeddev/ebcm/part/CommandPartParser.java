package me.fixeddev.ebcm.part;

import me.fixeddev.ebcm.internal.CommandLineParser;
import me.fixeddev.ebcm.stack.ArgumentStack;

public interface CommandPartParser {
    void parse(ArgumentStack stack, CommandLineParser parser, CommandPart part);
}
