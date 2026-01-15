package com.jemsire.utils;

import com.jemsire.plugin.DiscordWebhook;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class DiscordWebhookSender {

    public static void send(String content) {
        try {
            if(DiscordWebhook.get().getWebhookConfig().get().getWebhookLink().equalsIgnoreCase("PUT-WEBHOOK-URL-HERE")){
                DiscordWebhook.get().getLogger().at(Level.SEVERE).log("DISCORDWEBHOOK URL NOT SET! EDIT INSIDE THE CONFIG INSIDE YOUR MODS FOLDER!");
                return;
            }

            URL url = new URL(DiscordWebhook.get().getWebhookConfig().get().getWebhookLink());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // JSON body
            String json = "{\"content\": \"" + escape(content) + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            connection.getInputStream().close(); // perform send
        } catch (Exception e) {
            Logger.log(e.toString(), Level.SEVERE);
        }
    }

    public static void sendEmbed(String title, String description, String imageUrl, Integer color) {
        StringBuilder embed = new StringBuilder();
        embed.append("{ \"embeds\": [{");

        boolean first = true;

        if (title != null && !title.isEmpty()) {
            embed.append("\"title\": \"").append(escape(title)).append("\"");
            first = false;
        }

        if (description != null && !description.isEmpty()) {
            if (!first) embed.append(",");
            embed.append("\"description\": \"").append(escape(description)).append("\"");
            first = false;
        }

        if (color != null) {
            if (!first) embed.append(",");
            embed.append("\"color\": ").append(color);
            first = false;
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!first) embed.append(",");
            embed.append("\"image\": { \"url\": \"").append(escape(imageUrl)).append("\" }");
        }

        embed.append("}] }");

        sendRaw(embed.toString());
    }

    public static void sendMessage(String message) {
        send(message);
    }

    public static void sendRaw(String json) {
        try {
            if (DiscordWebhook.get().getWebhookConfig().get().getWebhookLink().equalsIgnoreCase("PUT-WEBHOOK-URL-HERE")) {
                Logger.log("DISCORDWEBHOOK URL NOT SET!", Level.SEVERE);
                return;
            }

            URL url = new URL(DiscordWebhook.get().getWebhookConfig().get().getWebhookLink());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            connection.getInputStream().close();
        } catch (Exception e) {
            Logger.log(e.toString(), Level.SEVERE);
        }
    }

    private static String escape(String msg) {
        return msg.replace("\"", "\\\"");
    }
}
