package com.jemsire.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderReplacer {
    // Match placeholders like {player}, {message}, {time}, {time-america/los_angeles}, etc.
    // Supports simple identifiers and timezone-specific time placeholders
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+(?:-[a-zA-Z0-9_/]+)?)\\}");

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
            
            String replacement = null;
            
            // Check if this is a timezone-specific time placeholder (e.g., time-america/los_angeles)
            if (placeholder.startsWith("time-") && placeholder.length() > 5) {
                String timezoneKey = placeholder.substring(5); // Remove "time-" prefix
                replacement = resolveTimezoneTime(timezoneKey, placeholders);
            } else {
                // Regular placeholder lookup
                replacement = placeholders.get(placeholder);
            }
            
            if (replacement == null) {
                // Placeholder not found, keep original
                matcher.appendReplacement(result, Matcher.quoteReplacement(fullMatch));
                continue;
            }
            
            // Escape the replacement for JSON (escape quotes, newlines, etc.)
            String escapedReplacement = escapeJson(replacement);
            
            // Replace the entire match (including braces) with the escaped replacement
            // Use quoteReplacement to handle any $ or \ characters in the replacement
            matcher.appendReplacement(result, Matcher.quoteReplacement(escapedReplacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }
    
    /**
     * Resolves a timezone-specific time placeholder
     * @param timezoneKey The timezone identifier (e.g., "america/los_angeles")
     * @param placeholders The placeholders map (used to get base time format)
     * @return Formatted time string for the timezone, or UTC if timezone is invalid
     */
    private static String resolveTimezoneTime(String timezoneKey, Map<String, String> placeholders) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        
        // Try to find the timezone by case-insensitive comparison
        String validTimezoneId = findValidTimezoneId(timezoneKey);
        
        if (validTimezoneId == null) {
            // Timezone not found, use UTC and log warning
            Logger.warning("Timezone '" + timezoneKey + "' does not exist, defaulting to UTC");
            ZonedDateTime utcTime = ZonedDateTime.now(ZoneId.of("UTC"));
            return utcTime.format(formatter);
        }
        
        // Timezone found, generate time for it
        try {
            ZoneId zoneId = ZoneId.of(validTimezoneId);
            ZonedDateTime zonedTime = ZonedDateTime.now(zoneId);
            return zonedTime.format(formatter);
        } catch (Exception e) {
            // Fallback to UTC if something goes wrong
            Logger.warning("Failed to use timezone '" + validTimezoneId + "', defaulting to UTC: " + e.getMessage());
            ZonedDateTime utcTime = ZonedDateTime.now(ZoneId.of("UTC"));
            return utcTime.format(formatter);
        }
    }
    
    /**
     * Finds a valid timezone ID by case-insensitive comparison
     * @param timezoneKey The timezone key to search for (lowercase)
     * @return Valid timezone ID if found, null otherwise
     */
    private static String findValidTimezoneId(String timezoneKey) {
        String lowerKey = timezoneKey.toLowerCase();
        
        // Get all available timezone IDs
        for (String zoneId : ZoneId.getAvailableZoneIds()) {
            if (zoneId.toLowerCase().equals(lowerKey)) {
                return zoneId; // Found exact match (case-insensitive)
            }
        }
        
        return null; // Timezone not found
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
