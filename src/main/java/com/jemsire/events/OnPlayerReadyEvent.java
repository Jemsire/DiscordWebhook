package com.jemsire.events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.jemsire.utils.DiscordWebhookSender;

public class OnPlayerReadyEvent {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();

        DiscordWebhookSender.sendEmbed(
                "📥 Player Joined",
                player.getDisplayName() + " has entered the world!",
                "",
                0x00FF00 // green
        );
    }

}