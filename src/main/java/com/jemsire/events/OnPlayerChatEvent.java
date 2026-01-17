package com.jemsire.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.jemsire.utils.DiscordWebhookSender;

import java.util.HashMap;
import java.util.Map;

public class OnPlayerChatEvent {
    public static void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String rawContent = event.getContent();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", sender.getUsername());
        placeholders.put("playerUsername", sender.getUsername()); // Alias for clarity
        placeholders.put("message", rawContent);
        placeholders.put("content", rawContent); // Alias for clarity

        DiscordWebhookSender.sendEventMessage("PlayerChat", placeholders);
    }
}