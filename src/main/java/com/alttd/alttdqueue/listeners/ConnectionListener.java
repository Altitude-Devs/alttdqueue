package com.alttd.alttdqueue.listeners;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.data.QueueResponse;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.alttd.alttdqueue.managers.ServerManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

public class ConnectionListener {

    private final AlttdQueue plugin;
    private final ServerManager serverManager;

    @Contract(pure = true)
    public ConnectionListener(AlttdQueue plugin) {
        this.plugin = plugin;
        this.serverManager = plugin.getServerManager();
    }

    @Subscribe
    public void onConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        ServerWrapper currentServer = serverManager.getServer(player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer() : null);
        Optional<RegisteredServer> server = event.getResult().getServer();
        if (server.isEmpty())
            return;
        ServerWrapper wrapper = serverManager.getServer(server.get());
        ServerPreConnectEvent.ServerResult result = event.getResult();
        String serverName = wrapper.getServerInfo().getName().toLowerCase();

        // check if they are whitelisted
        if (wrapper.hasWhiteList()) {
            if ((!player.hasPermission(Config.BYPASS_WHITELIST))
                    && !player.hasPermission(Config.WHITELIST + "." + serverName)) {
                if (currentServer == null) // if they aren't on a server yet send them to lobby
                    event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverManager.getLobby()));
                else // if they are on a server keep them there
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                player.sendMessage(MiniMessage.get().parse(Messages.NOT_WHITELISTED, Template.of("server", serverName)));
                return;
            }
        }

        // if they can skip the queue, we don't need to worry about them
        if (player.hasPermission(Config.SKIP_QUEUE)) {
            return;
        }

        if (wrapper.hasQueue()) {
            // if they are from outside the network and their target has a queue, boot them to lobby and add them to queue
            if (currentServer == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverManager.getLobby()));
                wrapper.addQueue(player);
                player.sendMessage(MiniMessage.get().parse(Config.DIRECT_CONNECT_FULL
                                .replace("{server}", wrapper.getServerInfo().getName())
                                .replace("{position}", wrapper.getPosition(player.getUniqueId())+""))
                        );
                return;
            }
            if (wrapper.isNextInQueue(player)) {
                return;
            }
        }

        // if it's not a lobby, or it is a lobby but it's full...
        if (!wrapper.isLobby()) {
            // if they try to connect to the server they're already on, we don't care
            if (currentServer == wrapper) {
                return;
            }
            ServerWrapper previousQueue = serverManager.getQueuedServer(player.getUniqueId());

            QueueResponse response = wrapper.addQueue(player);
            //AltiQueue.getInstance().send(wrapper.getServerInfo().getName(), wrapper.getQueuedPlayerList());
            if (response == QueueResponse.NOT_FULL || response == QueueResponse.SKIP_QUEUE) {
                return;
            }

            // check if they're already in queue
            if (response == QueueResponse.ALREADY_ADDED) {
                player.sendMessage(MiniMessage.get().parse(Config.ALREADY_QUEUED
                                .replace("{server}", wrapper.getServerInfo().getName())
                                .replace("{position}", wrapper.getPosition(player.getUniqueId())+"")));
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            // if they had a queue before, let them know
            if (previousQueue != null) {
                previousQueue.removeFromQueue(player.getUniqueId());
                player.sendMessage(MiniMessage.get().parse(Config.LEFT_QUEUE
                                .replace("{server}", wrapper.getServerInfo().getName())));
            }


            // if they're a new connection, send them to the lobby no matter what
            if (currentServer == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverManager.getLobby()));
                player.sendMessage(MiniMessage.get().parse(Config.DIRECT_CONNECT_FULL
                                .replace("{server}", wrapper.getServerInfo().getName())
                                .replace("{position}", wrapper.getPosition(player.getUniqueId())+"")));
                return;
            }

            // cancel the join event
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

            // tell them they were added to the queue
            player.sendMessage(MiniMessage.get().parse(Config.JOINED_QUEUE
                            .replace("{server}", wrapper.getServerInfo().getName())
                            .replace("{position}", wrapper.getPosition(player.getUniqueId())+"")));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        ServerWrapper wrapper = serverManager.getQueuedServer(event.getPlayer().getUniqueId());

        if (wrapper != null) {
            wrapper.removeFromQueue(event.getPlayer().getUniqueId());
        }
    }
}
