package me.fixeddev.ebcm.input;

import java.util.List;

public interface InputTokenizer {
    List<String> tokenize(String line);
}
