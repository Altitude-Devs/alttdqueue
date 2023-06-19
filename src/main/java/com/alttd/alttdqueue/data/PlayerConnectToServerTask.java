package com.alttd.alttdqueue.data;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.listeners.ConnectionListener;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.concurrent.TimeUnit;

public class PlayerConnectToServerTask implements Runnable {
    private final QueuePlayer queuePlayer;
    private final Player player;
    private final ServerWrapper serverWrapper;
    private final AlttdQueue plugin;
    private final ConnectionListener connectionListener;
    public PlayerConnectToServerTask(QueuePlayer queuePlayer, Player player, ServerWrapper serverWrapper, AlttdQueue plugin, ConnectionListener connectionListener) {
        this.queuePlayer = queuePlayer;
        this.player = player;
        this.serverWrapper = serverWrapper;
        this.plugin = plugin;
        this.connectionListener = connectionListener;
    }

    @Override
    public void run() {
        tryConnect(0);
    }

    private void tryConnect(int attempt) {
        connectionListener.addAllowed(player.getUniqueId());
        player.createConnectionRequest(serverWrapper.getRegisteredServer()).connect().thenAccept(
                result -> {
                    if (result == null) {
                        plugin.getLogger().warn("Received null result");
                        plugin.getLogger().warn("Waiting 5 seconds");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        retry(attempt);
                        return;
                    }
                    switch (result.getStatus()) {

                        case SUCCESS -> {
                            plugin.getLogger().info("Received success result when connecting player from queue to a server");
                            player.sendMessage(MiniMessage.miniMessage().deserialize(Messages.SENDING_TO_SERVER,
                                    TagResolver.resolver(
                                            Placeholder.parsed("server", serverWrapper.getRegisteredServer().getServerInfo().getName()),
                                            Placeholder.parsed("time", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - queuePlayer.queueJoinTime()))))));
                        }
                        case ALREADY_CONNECTED -> {
                            plugin.getLogger().info("Received already connected result when connecting player from queue to a server");
                        }
                        case CONNECTION_IN_PROGRESS -> {
                            plugin.getLogger().info("Received connection in progress result when connecting player from queue to a server");
                            plugin.getLogger().info("Waiting 5 seconds...");
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            retry(attempt);
                        }
                        case CONNECTION_CANCELLED, SERVER_DISCONNECTED -> {
                            plugin.getLogger().info("Received " + result.getStatus().name() + " result when connecting player from queue to a server");
                            retry(attempt);
                        }
                    }
                }
        );
    }

    private void retry(int attempt) {
        plugin.getLogger().info("Retry attempt " + attempt);
        if (attempt > 3) {
            serverWrapper.playerLeaveServer(player.getUniqueId());
            player.disconnect(MiniMessage.miniMessage().deserialize("<red>Queue failed to connect you, please rejoin the server and queue.</red>")); //TODO config
        } else {
            tryConnect(attempt + 1);
        }
    }
}
