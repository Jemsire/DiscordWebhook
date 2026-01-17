package com.jemsire.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Jemsire/DiscordWebhook/releases/latest";
    private static final String GITHUB_RELEASES_URL = "https://github.com/Jemsire/DiscordWebhook/releases";
    
    private final String currentVersion;
    
    public UpdateChecker(String currentVersion) {
        this.currentVersion = currentVersion;
    }
    
    /**
     * Checks for updates asynchronously
     * @deprecated Use checkForUpdates() from thread pool instead
     */
    @Deprecated
    public void checkForUpdatesAsync() {
        // Run in a separate thread to avoid blocking startup
        Thread updateThread = new Thread(() -> {
            try {
                checkForUpdates();
            } catch (Exception e) {
                // Silently fail - update checking shouldn't break the plugin
                Logger.log("Update check failed: " + e.getMessage(), Level.WARNING);
            }
        });
        updateThread.setName("DiscordWebhook-UpdateChecker");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    /**
     * Checks for updates synchronously
     * Can be called from any thread - uses Logger utility for thread-safe logging
     */
    public void checkForUpdates() {
        try {
            String latestVersion = fetchLatestVersion();
            
            if (latestVersion == null) {
                Logger.info("Could not fetch latest version from GitHub");
                return;
            }
            
            if (isNewerVersion(latestVersion, currentVersion)) {
                Logger.info("═══════════════════════════════════════════════════════════");
                Logger.info("A new version of DiscordWebhook is available!");
                Logger.info("Current version: " + currentVersion);
                Logger.info("Latest version: " + latestVersion);
                Logger.info("Download: " + GITHUB_RELEASES_URL);
                Logger.info("═══════════════════════════════════════════════════════════");
            } else {
                Logger.info("DiscordWebhook is up to date! (Version: " + currentVersion + ")");
            }
        } catch (Exception e) {
            Logger.severe("Update check failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fetches the latest release version from GitHub API
     */
    private String fetchLatestVersion() {
        try {
            URL url = new URL(GITHUB_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setConnectTimeout(5000); // 5 second timeout
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            
            // Parse JSON to extract tag_name
            // Simple regex approach - looking for "tag_name":"v1.0.0" or "tag_name":"1.0.0"
            Pattern pattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response.toString());
            
            if (matcher.find()) {
                String tagName = matcher.group(1);
                // Remove 'v' prefix if present (e.g., "v1.0.0" -> "1.0.0")
                return tagName.startsWith("v") ? tagName.substring(1) : tagName;
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Compares two version strings to determine if the first is newer than the second
     * Supports semantic versioning (e.g., 1.0.0, 1.0.1, 2.0.0)
     */
    private boolean isNewerVersion(String version1, String version2) {
        try {
            // Remove any non-numeric/dot characters (like -SNAPSHOT)
            String clean1 = version1.split("-")[0];
            String clean2 = version2.split("-")[0];
            
            String[] parts1 = clean1.split("\\.");
            String[] parts2 = clean2.split("\\.");
            
            int maxLength = Math.max(parts1.length, parts2.length);
            
            for (int i = 0; i < maxLength; i++) {
                int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
                int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
                
                if (part1 > part2) {
                    return true;
                } else if (part1 < part2) {
                    return false;
                }
            }
            
            // If versions are equal, check if one has -SNAPSHOT suffix
            // SNAPSHOT versions are considered older than release versions
            boolean v1Snapshot = version1.contains("-SNAPSHOT");
            boolean v2Snapshot = version2.contains("-SNAPSHOT");
            
            if (v1Snapshot && !v2Snapshot) {
                return false; // v1 is snapshot, v2 is release
            } else return !v1Snapshot && v2Snapshot; // v1 is release, v2 is snapshot
// Versions are equal
        } catch (Exception e) {
            // If version comparison fails, assume versions are equal
            return false;
        }
    }
}
