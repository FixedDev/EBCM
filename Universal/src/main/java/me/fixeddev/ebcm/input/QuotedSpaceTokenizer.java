package me.fixeddev.ebcm.input;

import java.util.ArrayList;
import java.util.List;

public class QuotedSpaceTokenizer implements InputTokenizer {
    @Override
    public List<String> tokenize(String line) {
        List<String> inputTokens = new ArrayList<>();

        StringBuilder token = new StringBuilder();

        boolean quoted = false;
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char charAt = line.charAt(i);

            if (charAt == '\\') {
                escaped = true;
            }

            if ((charAt == '"' || charAt == '\'') && !escaped) {
                quoted = !quoted;
                escaped = false;
                continue;
            }

            if (charAt == ' ' && !quoted) {
                inputTokens.add(token.toString());
            }

            token.append(charAt);
            escaped = false;
        }

        return inputTokens;
    }
}
