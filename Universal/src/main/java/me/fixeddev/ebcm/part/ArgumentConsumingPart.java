package me.fixeddev.ebcm.part;

import java.util.List;

public interface ArgumentConsumingPart extends LineConsumingPart {
    Class<?> getArgumentType();

    /**
     * This default values are supposed to replace the command line arguments
     * in case that we need the default value
     *
     * @return A list of arguments that are used as default value
     */
    List<String> getDefaultValues();
}
