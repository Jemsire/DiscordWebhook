package com.jemsire.plugin;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.jemsire.commands.ReloadCommand;
import com.jemsire.config.EventConfig;
import com.jemsire.config.EventConfigManager;
import com.jemsire.config.WebhookConfig;
import com.jemsire.events.*;
import com.jemsire.utils.Logger;
import com.jemsire.utils.UpdateChecker;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DiscordWebhook extends JavaPlugin {
    private static DiscordWebhook instance;

    private final Semver version;

    public static DiscordWebhook get() {
        return instance;
    }

    private final Config<WebhookConfig> config;
    private final ExecutorService threadPool;

    public DiscordWebhook(@Nonnull JavaPluginInit init) {
        super(init);
        Logger.info("Starting Plugin...");

        version = init.getPluginManifest().getVersion();

        // Registers the configuration with the filename "WebhookConfig"
        this.config = this.withConfig("WebhookConfig", WebhookConfig.CODEC);

        // Copy default event files from resources if they don't exist
        copyDefaultEventFiles();

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
    }

    @Override
    protected void start() {
//        if (isJemPlaceholdersEnabled()) {
//            JemPlaceholdersAPI.registerExpansion(new JemAnnouncementsExpansion());
//        }

        if(config.get().getUpdateCheck()){
            new UpdateChecker(version.toString()).checkForUpdatesAsync();
        }

        Logger.info("[JemAnnouncements] Started!");
        Logger.info("[JemAnnouncements] Use /jemp help for commands");
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

        // Shutdown updater
        if(config.get().getUpdateCheck()){
            UpdateChecker.shutdown();
        }

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
        //this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, OnPlayerReadyEvent::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, OnPlayerChatEvent::onPlayerChat);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, OnPlayerDisconnectEvent::onPlayerDisconnect);
        this.getEventRegistry().registerGlobal(ShutdownEvent.class, OnShutdownEvent::onShutdownEvent);
        this.getEventRegistry().registerGlobal(BootEvent.class, OnBootEvent::onBootEvent);
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnectEvent::onPlayerConnect);
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
    public Config<EventConfig> createEventConfig(String configName, BuilderCodec<EventConfig> codec) {
        return this.withConfig(configName, codec);
    }

    /**
     * Copies default event files from resources/events/ to the server's events directory
     * Only copies files that don't already exist in the server directory
     */
    private void copyDefaultEventFiles() {
        try {
            File pluginDataDir = this.getDataDirectory().toFile();
            File eventsDir = new File(pluginDataDir, "events");

            // Create events directory if it doesn't exist
            if (!eventsDir.exists()) {
                eventsDir.mkdirs();
            }

            // List of default event files to copy from resources
            String[] defaultEventFiles = {
                    "PlayerChat.json",
                    //"PlayerReady.json",
                    "PlayerDisconnect.json",
                    "PlayerConnect.json",
                    "PlayerDeath.json",
                    "Shutdown.json",
                    "Boot.json"
            };

            // Copy each default file if it doesn't exist
            for (String fileName : defaultEventFiles) {
                copyDefaultEventFile(eventsDir, fileName);
            }
        } catch (Exception e) {
            Logger.warning("Failed to copy default event files: " + e.getMessage());
        }
    }

    /**
     * Copies a default event file from resources to the events directory if it doesn't exist
     * @param eventsDir The events directory in the server's plugin data folder
     * @param fileName The name of the file to copy (e.g., "PlayerChat.json")
     */
    private void copyDefaultEventFile(File eventsDir, String fileName) {
        File targetFile = new File(eventsDir, fileName);

        // Skip if file already exists
        if (targetFile.exists()) {
            return;
        }

        try {
            // Read from resources
            String resourcePath = "events/" + fileName;
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

            if (resourceStream == null) {
                Logger.warning("Default event file not found in resources: " + resourcePath);
                return;
            }

            // Copy to target location
            Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            resourceStream.close();

            Logger.info("Created default event file: " + targetFile.getAbsolutePath());
        } catch (Exception e) {
            Logger.warning("Failed to copy default event file " + fileName + ": " + e.getMessage());
        }
    }
}