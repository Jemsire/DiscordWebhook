package com.jemsire.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UpdateChecker {

    private static final String GITHUB_API_URL =
            "https://api.github.com/repos/Jemsire/DiscordWebhook/releases/latest";

    private static final String GITHUB_RELEASES_URL =
            "https://github.com/Jemsire/DiscordWebhook/releases";

    private static final String USER_AGENT =
            "DiscordWebhook-UpdateChecker";

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

    private static final ExecutorService UPDATE_EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "DiscordWebhook-UpdateChecker");
                t.setDaemon(true);
                return t;
            });

    private final String currentVersion;

    public UpdateChecker(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    /**
     * Entry point to call from plugin startup.
     * Never blocks the main thread.
     */
    public void checkForUpdatesAsync() {
        UPDATE_EXECUTOR.execute(this::checkForUpdatesInternal);
    }

    /**
     * Internal synchronous logic.
     * Runs ONLY on the update executor.
     */
    private void checkForUpdatesInternal() {
        try {
            Logger.info("Checking for updates...");

            String latestVersion = fetchLatestVersion();

            if (latestVersion == null) {
                Logger.info("Could not fetch latest version from GitHub");
                return;
            }

            if (isNewerVersion(latestVersion, currentVersion)) {
                logUpdateAvailable(latestVersion);
            } else {
                Logger.info("DiscordWebhook is up to date! (Version: " + currentVersion + ")");
            }
        } catch (Exception e) {
            Logger.warning("Update check failed: " + e.getMessage());
        }
    }

    /**
     * HTTP call
     */
    private String fetchLatestVersion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_API_URL))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            return extractTagName(response.body());
        } catch (Exception e) {
            return null;
        }
    }

    private String extractTagName(String json) {
        Matcher matcher = Pattern
                .compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(json);

        if (!matcher.find()) {
            return null;
        }

        String tag = matcher.group(1);
        return tag.startsWith("v") ? tag.substring(1) : tag;
    }

    private boolean isNewerVersion(String latest, String current) {
        try {
            String cleanLatest = latest.split("-")[0];
            String cleanCurrent = current.split("-")[0];

            String[] lParts = cleanLatest.split("\\.");
            String[] cParts = cleanCurrent.split("\\.");

            int max = Math.max(lParts.length, cParts.length);

            for (int i = 0; i < max; i++) {
                int l = i < lParts.length ? Integer.parseInt(lParts[i]) : 0;
                int c = i < cParts.length ? Integer.parseInt(cParts[i]) : 0;

                if (l > c) return true;
                if (l < c) return false;
            }

            boolean latestSnapshot = latest.contains("-SNAPSHOT");
            boolean currentSnapshot = current.contains("-SNAPSHOT");

            return currentSnapshot && !latestSnapshot;
        } catch (Exception e) {
            return false;
        }
    }

    private void logUpdateAvailable(String latestVersion) {
        Logger.info("═══════════════════════════════════════════════════════════");
        Logger.info("A new version of DiscordWebhook is available!");
        Logger.info("Current version: " + currentVersion);
        Logger.info("Latest version: " + latestVersion);
        Logger.info("Download: " + GITHUB_RELEASES_URL);
        Logger.info("═══════════════════════════════════════════════════════════");
    }

    /**
     * Call from plugin disable if Hytale provides lifecycle hooks.
     */
    public static void shutdown() {
        UPDATE_EXECUTOR.shutdownNow();
    }
}
