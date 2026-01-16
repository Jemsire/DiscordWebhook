package com.jemsire.plugin;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.jemsire.commands.ReloadCommand;
import com.jemsire.config.WebhookConfig;
import com.jemsire.events.OnPlayerChatEvent;
import com.jemsire.events.OnPlayerDeathEvent;
import com.jemsire.events.OnPlayerDisconnectEvent;
import com.jemsire.events.OnPlayerReadyEvent;
import com.jemsire.utils.UpdateChecker;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordWebhook extends JavaPlugin {
    private static DiscordWebhook instance;

    public static DiscordWebhook get() {
        return instance;
    }

    private final Config<WebhookConfig> config;

    public DiscordWebhook(@Nonnull JavaPluginInit init) {
        super(init);
        getLogger().at(Level.INFO).log("Starting Plugin...");

        // Registers the configuration with the filename "WebhookConfig"
        this.config = this.withConfig("WebhookConfig", WebhookConfig.CODEC);
    }

    @Override
    protected void setup() {
        instance = this;

        // Register commands
        registerCommands();

        // Register events
        registerEvents();

        getLogger().at(Level.INFO).log("Setup Finished.");

        config.save();
        getLogger().at(Level.INFO).log("Config Saved.");

        if(config.get().getUpdateCheck()){
            getLogger().at(Level.INFO).log("Checking for updates...");
            checkForUpdates();
        }
    }

    @Override
    protected void shutdown(){
        this.getCommandRegistry().shutdown();
        this.getEventRegistry().shutdown();

        getLogger().at(Level.INFO).log("Shutting down...");

        config.save();
        getLogger().at(Level.INFO).log("Config Saved.");

        getLogger().at(Level.INFO).log("Shutdown Complete");
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new ReloadCommand("dw-reload", "Reload the config for DiscordWebhook"));
        getLogger().at(Level.INFO).log("Commands Registered.");
    }

    private void registerEvents() {
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, OnPlayerReadyEvent::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, OnPlayerChatEvent::onPlayerChat);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, OnPlayerDisconnectEvent::onPlayerDisconnect);
        this.getEntityStoreRegistry().registerSystem(new OnPlayerDeathEvent());
        getLogger().at(Level.INFO).log("Events Registered.");
    }

    public Config<WebhookConfig> getWebhookConfig() {
        return this.config;
    }

    /**
     * Reads the version from manifest.json
     */
    private String getVersionFromManifest() {
        try {
            InputStream manifestStream = getClass().getClassLoader()
                    .getResourceAsStream("manifest.json");
            
            if (manifestStream == null) {
                return "1.0.0"; // fallback version
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(manifestStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            
            // Extract version using regex
            Pattern pattern = Pattern.compile("\"Version\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(content.toString());
            
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            getLogger().at(Level.WARNING).log("Failed to read version from manifest: " + e.getMessage());
        }
        
        return "1.0.0"; // fallback version
    }

    /**
     * Checks for plugin updates
     */
    private void checkForUpdates() {
        String currentVersion = getVersionFromManifest();
        UpdateChecker updateChecker = new UpdateChecker(currentVersion);
        updateChecker.checkForUpdates();
    }
}