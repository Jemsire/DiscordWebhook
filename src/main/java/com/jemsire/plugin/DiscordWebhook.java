package com.jemsire.plugin;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.jemsire.commands.ReloadCommand;
import com.jemsire.config.EventConfigManager;
import com.jemsire.config.WebhookConfig;
import com.jemsire.events.OnPlayerChatEvent;
import com.jemsire.events.OnPlayerDeathEvent;
import com.jemsire.events.OnPlayerDisconnectEvent;
import com.jemsire.events.OnPlayerReadyEvent;
import com.jemsire.utils.Logger;
import com.jemsire.utils.UpdateChecker;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordWebhook extends JavaPlugin {
    private static DiscordWebhook instance;

    public static DiscordWebhook get() {
        return instance;
    }

    private final Config<WebhookConfig> config;
    private final ExecutorService threadPool;

    public DiscordWebhook(@Nonnull JavaPluginInit init) {
        super(init);
        Logger.info("Starting Plugin...");

        // Registers the configuration with the filename "WebhookConfig"
        this.config = this.withConfig("WebhookConfig", WebhookConfig.CODEC);

        // Initialize event config system (Service-Storage pattern)
        EventConfigManager.initialize(this);
        
        // Initialize thread pool for async webhook operations
        this.threadPool = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r);
            thread.setName("DiscordWebhook-Thread-" + thread.threadId());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    protected void setup() {
        instance = this;

        // Register commands
        registerCommands();

        // Register events
        registerEvents();

        Logger.info("Setup Finished.");

        config.save();
        Logger.info("Config Saved.");

        if(config.get().getUpdateCheck()){
            Logger.info("Checking for updates...");
            // Run update check asynchronously to avoid blocking startup
            threadPool.execute(() -> {
                try {
                    checkForUpdates();
                } catch (Exception e) {
                    Logger.warning("Update check failed: " + e.getMessage());
                }
            });
        }
    }

    @Override
    protected void shutdown(){
        Logger.info("Shutting down...");

        // Shutdown thread pool gracefully
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    Logger.warning("Thread pool did not terminate gracefully, forcing shutdown...");
                    threadPool.shutdownNow();
                    if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        Logger.severe("Thread pool did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        this.getCommandRegistry().shutdown();
        this.getEventRegistry().shutdown();

        config.save();
        Logger.info("Config Saved.");

        Logger.info("Shutdown Complete");
    }
    
    /**
     * Gets the thread pool for async operations
     */
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new ReloadCommand("dw-reload", "Reload the config for DiscordWebhook"));
        Logger.info("Commands Registered.");
    }

    private void registerEvents() {
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, OnPlayerReadyEvent::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, OnPlayerChatEvent::onPlayerChat);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, OnPlayerDisconnectEvent::onPlayerDisconnect);
        this.getEntityStoreRegistry().registerSystem(new OnPlayerDeathEvent());
        Logger.info("Events Registered.");
    }

    public Config<WebhookConfig> getWebhookConfig() {
        return this.config;
    }

    /**
     * Creates an event config using the protected withConfig method
     * This allows EventConfigManager to create configs without direct access to withConfig
     */
    public com.hypixel.hytale.server.core.util.Config<com.jemsire.config.EventConfig> createEventConfig(String configName, com.hypixel.hytale.codec.builder.BuilderCodec<com.jemsire.config.EventConfig> codec) {
        return this.withConfig(configName, codec);
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
            Logger.warning("Failed to read version from manifest: " + e.getMessage());
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