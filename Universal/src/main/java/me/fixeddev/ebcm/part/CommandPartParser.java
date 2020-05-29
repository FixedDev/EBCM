package me.fixeddev.ebcm.part;

import me.fixeddev.ebcm.ParsingContext;

public interface CommandPartParser {
    void parse(CommandPart part, ParsingContext context);
}
