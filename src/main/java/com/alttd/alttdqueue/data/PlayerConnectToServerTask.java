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
    private final NewServerWrapper newServerWrapper;
    public PlayerConnectToServerTask(QueuePlayer queuePlayer, Player player, NewServerWrapper newServerWrapper) {
        this.queuePlayer = queuePlayer;
        this.player = player;
        this.newServerWrapper = newServerWrapper;
    }

    @Override
    public void run() {
        tryConnect(0);
    }

    private void tryConnect(int attempt) {
        player.createConnectionRequest(newServerWrapper.getRegisteredServer()).connect().thenAccept(
                result -> {
                    if (result == null || !result.isSuccessful()) {
                        if (attempt > 3) {
                            newServerWrapper.removeOnlinePlayerDueToError(player.getUniqueId());
                            player.disconnect(MiniMessage.miniMessage().deserialize("<red>Queue failed to connect you, please rejoin the server and queue.</red>")); //TODO config
                        } else {
                            tryConnect(attempt + 1);
                        }
                        return;
                    }
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Messages.SENDING_TO_SERVER,
                            TagResolver.resolver(
                                    Placeholder.parsed("server", newServerWrapper.getRegisteredServer().getServerInfo().getName()),
                                    Placeholder.parsed("time", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - queuePlayer.queueJoinTime()))))));
                }
        );
    }
}
