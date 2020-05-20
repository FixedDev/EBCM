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
                continue;
            }

            if ((charAt == '"' || charAt == '\'') && !escaped) {
                quoted = !quoted;
                escaped = false;
                continue;
            }

            escaped = false;

            if (charAt == ' ' && !quoted) {
                String tokenStr = token.toString();

                if(!tokenStr.trim().isEmpty()){
                    inputTokens.add(tokenStr);
                }

                continue;
            }

            token.append(charAt);
        }

        return inputTokens;
    }
}
