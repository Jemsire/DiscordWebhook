package com.jemsire.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.jemsire.plugin.DiscordWebhook;

import javax.annotation.Nonnull;
import java.awt.*;

public class ReloadCommand extends CommandBase {

    public ReloadCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        if(context.isPlayer()){
            if(!context.sender().hasPermission("discordwebhook.reload")){
                context.sendMessage(Message.raw("You do not have permission to perform this command!").color(Color.RED));
                return;
            }
        }

        if(DiscordWebhook.get().getWebhookConfig().get().reloadConfig()){
            context.sendMessage(Message.raw("Config reloaded with new values!").color(Color.GREEN));
            return;
        }

        context.sendMessage(Message.raw("Config has no changes. Config was not reloaded.").color(Color.RED));
    }
}
