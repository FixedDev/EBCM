package me.fixeddev.ebcm;

import java.util.List;

public interface SuggestionProvider {
    List<String> getSuggestions(String startsWith);
}
