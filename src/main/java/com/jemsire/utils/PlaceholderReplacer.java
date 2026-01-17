package com.jemsire.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderReplacer {
    // Match placeholders like {player}, {message}, etc.
    // Only matches simple identifiers (letters, numbers, underscores) to avoid matching JSON structure
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

    /**
     * Replaces placeholders in a JSON string with provided values
     * @param json The JSON string with placeholders like {player}, {message}, etc.
     * @param placeholders Map of placeholder names (without braces) to their values
     * @return JSON string with placeholders replaced
     */
    public static String replacePlaceholders(String json, Map<String, String> placeholders) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        if (placeholders == null || placeholders.isEmpty()) {
            return json;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(json);

        while (matcher.find()) {
            // matcher.group(0) is the full match including braces: "{player}"
            // matcher.group(1) is just the content inside braces: "player"
            String placeholder = matcher.group(1).trim(); // Trim whitespace
            String fullMatch = matcher.group(0);
            
            String originalValue = placeholders.get(placeholder);
            
            if (originalValue == null) {
                // Placeholder not found, keep original
                matcher.appendReplacement(result, Matcher.quoteReplacement(fullMatch));
                continue;
            }
            
            // Escape the replacement for JSON (escape quotes, newlines, etc.)
            String replacement = escapeJson(originalValue);
            
            // Replace the entire match (including braces) with the escaped replacement
            // Use quoteReplacement to handle any $ or \ characters in the replacement
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Escapes special characters for JSON
     */
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}
