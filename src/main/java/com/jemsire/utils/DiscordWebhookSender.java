package com.jemsire.utils;

import com.jemsire.config.EventConfig;
import com.jemsire.config.EventConfigManager;
import com.jemsire.config.WebhookConfig;
import com.jemsire.plugin.DiscordWebhook;
import com.hypixel.hytale.server.core.util.Config;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

public class DiscordWebhookSender {

    /**
     * Sends a message using an event config (asynchronously)
     * @param eventName The name of the event config to use
     * @param placeholders Map of placeholder values to replace in the JSON
     */
    public static void sendEventMessage(String eventName, Map<String, String> placeholders) {
        // Execute webhook send asynchronously to avoid blocking the main thread
        DiscordWebhook plugin = DiscordWebhook.get();
        if (plugin != null && plugin.getThreadPool() != null) {
            plugin.getThreadPool().execute(() -> sendEventMessageSync(eventName, placeholders));
        } else {
            // Fallback to sync if thread pool not available
            sendEventMessageSync(eventName, placeholders);
        }
    }
    
    /**
     * Synchronous version of sendEventMessage (internal use)
     */
    private static void sendEventMessageSync(String eventName, Map<String, String> placeholders) {
        Config<EventConfig> eventConfig = EventConfigManager.getEventConfig(eventName);
        
        if (eventConfig == null) {
            Logger.log("Event config not found: " + eventName, Level.WARNING);
            return;
        }

        EventConfig config = eventConfig.get();
        
        if (!config.isEnabled()) {
            return; // Event is disabled
        }

        String jsonTemplate = config.getMessageJson();
        if (jsonTemplate == null || jsonTemplate.isEmpty()) {
            Logger.log("Event config " + eventName + " has no MessageJson", Level.WARNING);
            return;
        }

        // Replace placeholders
        String json = PlaceholderReplacer.replacePlaceholders(jsonTemplate, placeholders);

        // Get webhook URL from channel
        WebhookConfig webhookConfig = DiscordWebhook.get().getWebhookConfig().get();
        String webhookUrl = webhookConfig.getWebhookForChannel(config.getWebhookChannel());

        sendRawSync(json, webhookUrl);
    }

    /**
     * Sends raw JSON to a webhook URL (asynchronously)
     */
    public static void sendRaw(String json, String webhookUrl) {
        DiscordWebhook plugin = DiscordWebhook.get();
        if (plugin != null && plugin.getThreadPool() != null) {
            plugin.getThreadPool().execute(() -> sendRawSync(json, webhookUrl));
        } else {
            // Fallback to sync if thread pool not available
            sendRawSync(json, webhookUrl);
        }
    }
    
    /**
     * Synchronous version of sendRaw (internal use)
     */
    private static void sendRawSync(String json, String webhookUrl) {
        try {
            if (webhookUrl == null || webhookUrl.isEmpty() || 
                webhookUrl.equalsIgnoreCase("PUT-WEBHOOK-URL-HERE")) {
                Logger.severe("DISCORDWEBHOOK URL NOT SET!");
                return;
            }

            URL url = new URL(webhookUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000); // 5 second timeout
            connection.setReadTimeout(5000); // 5 second timeout

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                // Success - read response to close connection
                connection.getInputStream().close();
            } else {
                Logger.log("Webhook returned error code: " + responseCode, Level.WARNING);
                connection.getErrorStream().close();
            }
        } catch (Exception e) {
            Logger.severe("Failed to send webhook", e);
        }
    }
}
