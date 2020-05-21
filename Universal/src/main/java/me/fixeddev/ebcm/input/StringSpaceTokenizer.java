package me.fixeddev.ebcm.input;

import java.util.Arrays;
import java.util.List;

public class StringSpaceTokenizer implements InputTokenizer {
    @Override
    public List<String> tokenize(String line) {
        return Arrays.asList(line.split(" "));
    }
}
