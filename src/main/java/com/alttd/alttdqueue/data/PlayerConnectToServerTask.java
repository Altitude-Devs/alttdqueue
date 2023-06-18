package com.alttd.alttdqueue.data;

import com.alttd.alttdqueue.config.Messages;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.concurrent.TimeUnit;

public class PlayerConnectToServerTask implements Runnable {
    private final QueuePlayer queuePlayer;
    private final Player player;
    private final ServerWrapper serverWrapper;
    public PlayerConnectToServerTask(QueuePlayer queuePlayer, Player player, ServerWrapper serverWrapper) {
        this.queuePlayer = queuePlayer;
        this.player = player;
        this.serverWrapper = serverWrapper;
    }

    @Override
    public void run() {
        tryConnect(0);
    }

    private void tryConnect(int attempt) {
        player.createConnectionRequest(serverWrapper.getRegisteredServer()).connect().thenAccept(
                result -> {
                    if (result == null || !result.isSuccessful()) {
                        if (attempt > 3) {
                            serverWrapper.playerLeaveServer(player.getUniqueId());
                            player.disconnect(MiniMessage.miniMessage().deserialize("<red>Queue failed to connect you, please rejoin the server and queue.</red>")); //TODO config
                        } else {
                            tryConnect(attempt + 1);
                        }
                        return;
                    }
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Messages.SENDING_TO_SERVER,
                            TagResolver.resolver(
                                    Placeholder.parsed("server", serverWrapper.getRegisteredServer().getServerInfo().getName()),
                                    Placeholder.parsed("time", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - queuePlayer.queueJoinTime()))))));
                }
        );
    }
}
