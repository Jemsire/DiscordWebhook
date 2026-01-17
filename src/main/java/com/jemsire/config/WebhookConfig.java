package com.jemsire.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.jemsire.plugin.DiscordWebhook;
import com.jemsire.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class WebhookConfig {
    private Integer version = 3;
    private String webhookLink = "PUT-WEBHOOK-URL-HERE";
    private boolean updateCheck = true;
    private Map<String, String> webhookChannels = new HashMap<>();

    public WebhookConfig() {
        // Default webhook channels
        webhookChannels.put("default", "PUT-WEBHOOK-URL-HERE");
        webhookChannels.put("staff", "PUT-WEBHOOK-URL-HERE");
    }

    public static final BuilderCodec<WebhookConfig> CODEC =
            BuilderCodec.builder(WebhookConfig.class, WebhookConfig::new)
                    .append(
                            new KeyedCodec<String>("WebhookLink", Codec.STRING),
                            (config, value, info) -> config.webhookLink = value,
                            (config, info) -> config.webhookLink
                    )
                    .add()

                    .append(
                            new KeyedCodec<Integer>("Version", Codec.INTEGER),
                            (config, value, info) -> config.version = value,
                            (config, info) -> config.version
                    )
                    .add()

                    .append(
                            new KeyedCodec<Boolean>("UpdateCheck", Codec.BOOLEAN),
                            (config, value, info) -> config.updateCheck = value,
                            (config, info) -> config.updateCheck
                    )
                    .add()

                    .append(
                            new KeyedCodec<Map<String, String>>("WebhookChannels", MapCodec.STRING_HASH_MAP_CODEC),
                            (config, value, info) -> config.webhookChannels = value != null ? value : new HashMap<>(),
                            (config, info) -> config.webhookChannels
                    )
                    .add()

                    .build();

    public String getWebhookLink() {
        return webhookLink;
    }

    public Boolean getUpdateCheck() {
        return updateCheck;
    }

    public Map<String, String> getWebhookChannels() {
        return webhookChannels;
    }

    public String getWebhookForChannel(String channel) {
        if (channel == null || channel.isEmpty()) {
            channel = "default";
        }
        String webhook = webhookChannels.get(channel.toLowerCase());
        if (webhook == null || webhook.isEmpty() || webhook.equalsIgnoreCase("PUT-WEBHOOK-URL-HERE")) {
            // Fallback to default webhook
            return webhookLink;
        }
        return webhook;
    }

    public boolean reloadConfig() {
        String original = DiscordWebhook.get().getWebhookConfig().get().getWebhookLink();
        String New;

        // Access new values
        DiscordWebhook.get().getWebhookConfig().load();
        New = DiscordWebhook.get().getWebhookConfig().get().getWebhookLink();

        //Check if config values changed
        if(!Objects.equals(New, original)){
            Logger.log("Config reloaded with new data!", Level.INFO);
            return true;
        }

        Logger.log("Config has not changed!", Level.INFO);
        return false;
    }
}
