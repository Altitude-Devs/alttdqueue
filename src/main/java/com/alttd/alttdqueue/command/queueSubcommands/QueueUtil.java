package com.alttd.alttdqueue.command.queueSubcommands;

import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class QueueUtil {

    public static void checkQueue(Player player, ServerWrapper serverWrapper) {
        if (serverWrapper == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.NOT_QUEUED));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.CHECK_STATUS
                    .replace("{server}", serverWrapper.getServerInfo().getName())
                    .replace("{position}", serverWrapper.getPosition(player.getUniqueId()) + "")));
        }
    }

}
