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
import com.jemsire.events.OnPlayerDisconnectEvent;
import com.jemsire.events.OnPlayerReadyEvent;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class DiscordWebhook extends JavaPlugin {
    private static DiscordWebhook instance;

    public static DiscordWebhook get() {
        return instance;
    }

    private final Config<WebhookConfig> config;

    public DiscordWebhook(@Nonnull JavaPluginInit init) {
        super(init);
        getLogger().at(Level.INFO).log("Starting Plugin...");

        // Registers the configuration with the filename "ExamplePlugin"
        this.config = this.withConfig("WebhookConfig", WebhookConfig.CODEC);
    }

    @Override
    protected void setup() {
        instance = this;

        // Register commands
        registerCommands();

        // Register events
        registerEvents();

        //WebhookConfig cfg = config.get();

        getLogger().at(Level.INFO).log("Setup Finished.");

        config.save();
        getLogger().at(Level.INFO).log("Config Saved.");
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
        getLogger().at(Level.INFO).log("Events Registered.");
    }

    public Config<WebhookConfig> getWebhookConfig() {
        return this.config;
    }


}