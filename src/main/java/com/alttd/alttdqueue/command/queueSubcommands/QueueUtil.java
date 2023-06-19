package com.alttd.alttdqueue.command.queueSubcommands;

import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.data.Priority;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class QueueUtil {

    public static void checkQueue(Player player, ServerWrapper serverWrapper) {
        if (serverWrapper == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.NOT_QUEUED));
        } else {
            String queueTypeMessage = getQueueTypeMessage(player, serverWrapper);
            if (queueTypeMessage == null) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(Config.NOT_QUEUED));
                return;
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.CHECK_STATUS
                    .replace("{server}", serverWrapper.getServerInfo().getName())
                    .replace("{position}", String.valueOf(serverWrapper.getPosition(player.getUniqueId())))
                    .replace("{queue_type_message}", queueTypeMessage)));
        }
    }

    private static String getQueueTypeMessage(Player player, ServerWrapper wrapper) {
        Priority priority = wrapper.getPriority(player.getUniqueId());
        if (priority == null)
            return "";
        switch (wrapper.getPriority(player.getUniqueId())) {
            case HIGH -> {
                return Config.JOINED_HIGH_PRIORITY;
            }
            case MID -> {
                return Config.JOINED_MID_PRIORITY;
            }
            case LOW -> {
                return Config.JOINED_LOW_PRIORITY;
            }
            default -> {
                return "";
            }
        }
    }
}
