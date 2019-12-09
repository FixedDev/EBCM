package me.fixeddev.ebcm.util;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.part.LineConsumingPart;

import java.util.stream.Collectors;

public class UsageBuilder {
    public static String getUsageForCommand(Command command, String label){
        String usage = command.getParts().stream().filter(part -> part instanceof LineConsumingPart)
                .map(part -> (LineConsumingPart) part)
                .map(part -> part.getLineRepresentation())
                .collect(Collectors.joining(" "))
                .replace("<command>", label);

        return !usage.isEmpty() ? label + " " + usage : label;
    }
}
