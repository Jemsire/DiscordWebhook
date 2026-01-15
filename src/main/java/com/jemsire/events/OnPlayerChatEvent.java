package com.jemsire.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.jemsire.utils.DiscordWebhookSender;

import java.awt.*;

public class OnPlayerChatEvent {
    public static void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String rawContent = event.getContent();

        DiscordWebhookSender.sendMessage("💬 **" + sender.getUsername() + "**: " + rawContent);
    }
}