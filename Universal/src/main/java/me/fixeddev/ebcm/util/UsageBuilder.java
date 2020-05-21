package me.fixeddev.ebcm.util;

import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.part.LineConsumingPart;
import me.fixeddev.ebcm.part.SubCommandPart;

import java.util.stream.Collectors;

public class UsageBuilder {

    public static String getUsageForCommand(Command parent, Command command, String label) {
        if(!command.getUsage().equals("_!!_NOT_OVERRIDE_!!_")){
            return command.getUsage().replace("<command>", label);
        }

        String parentUsage = null;

        if (command.equals(parent)) {
            return getUsageForCommand(null, command, label);
        }

        if (parent != null && !parent.getParts().isEmpty()) {
            if(parent.getParts().size() > 1 || !(parent.getParts().get(0) instanceof SubCommandPart)){
                parentUsage = getUsageForCommand(null, parent, label);
            }
        }

        String usage = command.getParts().stream()
                .filter(part -> part instanceof LineConsumingPart)
                .map(part -> (LineConsumingPart) part)
                .map(LineConsumingPart::getLineRepresentation)
                .collect(Collectors.joining(" "))
                .replace("<command>", label);

        if (parentUsage != null && !parentUsage.isEmpty()) {
            return parentUsage + " " + usage;
        }

        return !usage.isEmpty() ? label + " " + usage : label;
    }
}
