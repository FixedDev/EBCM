package me.fixeddev.ebcm.util;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.part.LineConsumingPart;
import me.fixeddev.ebcm.part.SubCommandPart;

import java.util.stream.Collectors;

public class UsageBuilder {

    public static String getUsageForCommand(Command parent, Command command, String label) {
        String parentUsage = null;

        if (parent != null && !parent.equals(command)) {
            parentUsage = getUsageForCommand(null, parent, label);
        }

        String usage = command.getParts().stream()
                .filter(part -> part instanceof LineConsumingPart && !(part instanceof SubCommandPart))
                .map(part -> (LineConsumingPart) part)
                .map(LineConsumingPart::getLineRepresentation)
                .collect(Collectors.joining(" "))
                .replace("<command>", label);

        if (parentUsage != null) {
            return parentUsage + " " + usage;
        }

        return !usage.isEmpty() ? label + " " + usage : label;
    }
}
