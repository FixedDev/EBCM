package me.fixeddev.ebcm;

import me.fixeddev.ebcm.part.ArgumentPart;
import me.fixeddev.ebcm.part.CommandPart;

import java.util.List;
import java.util.Map;

public interface ParseResult {
    String getLabel();

    List<String> getCommandLine();

    Command getMainCommand();

    Command getCommandToExecute();

    List<ParameterBinding> getBindings();

    Map<CommandPart, Object> getValueBindings();

    interface ParameterBinding {
        List<String> getRaw();

        CommandPart getBind();
    }
}
