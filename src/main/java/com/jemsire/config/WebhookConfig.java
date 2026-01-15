package com.jemsire.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.jemsire.plugin.DiscordWebhook;
import com.jemsire.utils.Logger;

import java.util.Objects;
import java.util.logging.Level;

public class WebhookConfig {
    private Integer version = 1;
    private String webhookLink = "PUT-WEBHOOK-URL-HERE";

    public static final BuilderCodec<WebhookConfig> CODEC =
            BuilderCodec.builder(WebhookConfig.class, WebhookConfig::new)
                    .append(
                            new KeyedCodec<>("WebhookLink", Codec.STRING),
                            (obj, val) -> obj.webhookLink = val,
                            obj -> obj.webhookLink
                    )
                    .add()
                    .append(
                            new KeyedCodec<>("Version", Codec.INTEGER),
                            (obj, val) -> obj.version = val,
                            obj -> obj.version
                    )
                    .add()
                    .build();

    public WebhookConfig() {
    }

    public String getWebhookLink() {
        return webhookLink;
    }

    public boolean reloadConfig() {
        String original = DiscordWebhook.get().getWebhookConfig().get().getWebhookLink();
        String New = "";

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
