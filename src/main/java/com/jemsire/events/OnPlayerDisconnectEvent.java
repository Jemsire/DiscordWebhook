package com.jemsire.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.jemsire.utils.DiscordWebhookSender;

public class OnPlayerDisconnectEvent {

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef player = event.getPlayerRef();

        DiscordWebhookSender.sendEmbed(
                "📤 Player Left",
                player.getUsername() + " has logged out.",
                "",
                0xFF0000 // red
        );
    }

}