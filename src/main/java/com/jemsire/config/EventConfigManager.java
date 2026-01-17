package com.jemsire.config;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.util.Config;
import com.jemsire.plugin.DiscordWebhook;
import com.jemsire.utils.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for event configurations
 * Handles loading, saving, caching, and default config creation
 */
public class EventConfigManager {
    private static final String EVENTS_FOLDER = "events";
    private static Map<String, Config<EventConfig>> cache;
    private static JavaPlugin plugin;
    private static Path eventsDir;

    /**
     * Initializes the event config system
     * @param pluginInstance The plugin instance
     */
    public static void initialize(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        cache = new ConcurrentHashMap<>();
        
        // Setup events directory
        Path dataDir = plugin.getDataDirectory();
        eventsDir = dataDir.resolve(EVENTS_FOLDER);
        
        try {
            if (!Files.exists(eventsDir)) {
                Files.createDirectories(eventsDir);
                Logger.info("Created events directory: " + eventsDir);
            }
        } catch (Exception e) {
            Logger.severe("Failed to create events directory", e);
        }
        
        // Create default configs if they don't exist
        createDefaultConfigs();
        
        // Load all configs
        loadAllEventConfigs();
        
        Logger.info("EventConfigManager initialized with " + cache.size() + " configs");
    }

    /**
     * Gets an event config by name
     * @param eventName The name of the event config
     * @return The config, or null if not found
     */
    public static Config<EventConfig> getEventConfig(String eventName) {
        if (eventName == null) {
            return null;
        }
        if (cache == null) {
            Logger.warning("EventConfigManager not initialized. Call EventConfigManager.initialize() first.");
            return null;
        }
        return cache.get(eventName.toLowerCase());
    }

    /**
     * Gets all loaded event configs
     * @return Map of all event configs
     */
    public static Map<String, Config<EventConfig>> getAllEventConfigs() {
        if (cache == null) {
            Logger.warning("EventConfigManager not initialized. Call EventConfigManager.initialize() first.");
            return Map.of();
        }
        return new HashMap<>(cache);
    }

    /**
     * Reloads all event configs from storage
     */
    public static void reloadAllEventConfigs() {
        if (cache == null) {
            Logger.warning("EventConfigManager not initialized. Call EventConfigManager.initialize() first.");
            return;
        }
        
        int reloaded = 0;
        for (Map.Entry<String, Config<EventConfig>> entry : cache.entrySet()) {
            try {
                entry.getValue().load(); // Reload existing config
                reloaded++;
            } catch (Exception e) {
                Logger.severe("Failed to reload event config: " + entry.getKey(), e);
            }
        }
        Logger.info("Reloaded " + reloaded + " event configs");
    }

    /**
     * Loads all event configs from the events folder
     */
    private static void loadAllEventConfigs() {
        cache.clear();
        
        try {
            if (!Files.exists(eventsDir)) {
                return;
            }

            // Scan for all JSON files in events directory
            Files.list(eventsDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String eventName = fileName.substring(0, fileName.length() - 5); // Remove .json
                        
                        Config<EventConfig> config = loadEventConfig(eventName);
                        if (config != null) {
                            cache.put(eventName.toLowerCase(), config);
                            Logger.info("Loaded event config: " + eventName);
                        }
                    });
        } catch (Exception e) {
            Logger.severe("Failed to load event configs", e);
        }
    }

    /**
     * Loads a single event config by name
     */
    private static Config<EventConfig> loadEventConfig(String eventName) {
        try {
            if (!(plugin instanceof DiscordWebhook discordWebhook)) {
                Logger.warning("Plugin is not an instance of DiscordWebhook, cannot load event config");
                return null;
            }

            Config<EventConfig> config = discordWebhook.createEventConfig(
                    EVENTS_FOLDER + "/" + eventName,
                    EventConfig.CODEC
            );
            config.load();
            config.save(); // Ensure config is properly initialized
            return config;
        } catch (Exception e) {
            Logger.severe("Failed to load event config " + eventName, e);
            return null;
        }
    }

    /**
     * Creates default event configs if they don't exist
     */
    private static void createDefaultConfigs() {
        Map<String, String> defaultConfigs = new HashMap<>();
        
        // PlayerChat event - Example of plain text message
        defaultConfigs.put("PlayerChat", "{\n" +
                "  \"Enabled\": true,\n" +
                "  \"WebhookChannel\": \"default\",\n" +
                "  \"MessageJson\": \"{\\\"content\\\": \\\"💬 **{player}**: {message}\\\"}\"\n" +
                "}");

        // PlayerReady event - Example of embed message
        defaultConfigs.put("PlayerReady", "{\n" +
                "  \"Enabled\": true,\n" +
                "  \"WebhookChannel\": \"default\",\n" +
                "  \"MessageJson\": \"{\\\"embeds\\\": [{\\\"title\\\": \\\"📥 Player Joined\\\", \\\"description\\\": \\\"{player} has entered the world!\\\", \\\"color\\\": 65280}]}\"\n" +
                "}");

        // PlayerDisconnect event - Example of embed message
        defaultConfigs.put("PlayerDisconnect", "{\n" +
                "  \"Enabled\": true,\n" +
                "  \"WebhookChannel\": \"default\",\n" +
                "  \"MessageJson\": \"{\\\"embeds\\\": [{\\\"title\\\": \\\"📤 Player Left\\\", \\\"description\\\": \\\"{player} has logged out.\\\", \\\"color\\\": 16711680}]}\"\n" +
                "}");

        // PlayerDeath event - Example of embed message
        defaultConfigs.put("PlayerDeath", "{\n" +
                "  \"Enabled\": true,\n" +
                "  \"WebhookChannel\": \"default\",\n" +
                "  \"MessageJson\": \"{\\\"embeds\\\": [{\\\"title\\\": \\\"☠ Player Died\\\", \\\"description\\\": \\\"{player} {deathCause}\\\", \\\"color\\\": 12434877}]}\"\n" +
                "}");

        for (Map.Entry<String, String> entry : defaultConfigs.entrySet()) {
            Path configPath = eventsDir.resolve(entry.getKey() + ".json");
            if (!Files.exists(configPath)) {
                try {
                    Files.write(configPath, entry.getValue().getBytes());
                    Logger.info("Created default event config: " + entry.getKey());
                } catch (Exception e) {
                    Logger.severe("Failed to create default config for " + entry.getKey(), e);
                }
            }
        }
    }
}
