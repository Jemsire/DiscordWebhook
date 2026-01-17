package com.jemsire.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Configuration for individual Discord webhook events.
 * 
 * MessageJson supports both plain text and embed formats:
 * 
 * Plain text example:
 *   {"content": "💬 **{player}**: {message}"}
 * 
 * Embed example:
 *   {"embeds": [{"title": "Title", "description": "Description with {placeholders}", "color": 65280}]}
 * 
 * Combined (text + embed):
 *   {"content": "Text message", "embeds": [{"title": "Embed Title", "description": "Embed description"}]}
 * 
 * Use {placeholderName} in your JSON strings, and they will be replaced at runtime.
 * 
 * Available placeholders by event type:
 * 
 * PlayerChat:
 *   - {player}, {playerUsername} - Player's username
 *   - {message}, {content} - Chat message content
 *   - {playerUuid} - Player's UUID (if available)
 * 
 * PlayerReady:
 *   - {player}, {playerDisplayName} - Player's display name
 *   - {playerUsername} - Player's username (if available)
 *   - {playerUuid} - Player's UUID (if available)
 * 
 * PlayerDisconnect:
 *   - {player}, {playerUsername} - Player's username
 *   - {playerUuid} - Player's UUID (if available)
 * 
 * PlayerDeath:
 *   - {player}, {playerDisplayName} - Player's display name
 *   - {playerUsername} - Player's username (if available)
 *   - {playerUuid} - Player's UUID (if available)
 *   - {deathCause}, {deathMessage} - Formatted death message
 *   - {deathMessageRaw} - Raw death message (if available)
 */
public class EventConfig {
    private boolean enabled = true;
    private String webhookChannel = "default";
    private String messageJson = ""; // Raw JSON with placeholders - supports both plain text and embeds

    public EventConfig() {
    }

    public EventConfig(boolean enabled, String webhookChannel, String messageJson) {
        this.enabled = enabled;
        this.webhookChannel = webhookChannel;
        this.messageJson = messageJson;
    }

    public static final BuilderCodec<EventConfig> CODEC =
            BuilderCodec.builder(EventConfig.class, EventConfig::new)
                    .append(
                            new KeyedCodec<Boolean>("Enabled", Codec.BOOLEAN),
                            (config, value, info) -> config.enabled = value != null ? value : true,
                            (config, info) -> config.enabled
                    )
                    .add()

                    .append(
                            new KeyedCodec<String>("WebhookChannel", Codec.STRING),
                            (config, value, info) -> config.webhookChannel = value != null ? value : "default",
                            (config, info) -> config.webhookChannel
                    )
                    .add()

                    .append(
                            new KeyedCodec<String>("MessageJson", Codec.STRING),
                            (config, value, info) -> config.messageJson = value != null ? value : "",
                            (config, info) -> config.messageJson
                    )
                    .add()

                    .build();

    public boolean isEnabled() {
        return enabled;
    }

    public String getWebhookChannel() {
        return webhookChannel;
    }

    public String getMessageJson() {
        return messageJson;
    }
}
