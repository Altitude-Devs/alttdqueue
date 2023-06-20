package com.alttd.alttdqueue.listeners;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.data.QueueResponse;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.alttd.alttdqueue.managers.ServerManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class ConnectionListener {

    private final AlttdQueue plugin;
    private ServerManager serverManager;

    @Contract(pure = true)
    public ConnectionListener(AlttdQueue plugin) {
        this.plugin = plugin;
    }

    public void init(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        event.getPreviousServer().ifPresent(prevServer -> {
            ServerWrapper server = serverManager.getServer(prevServer);
            if (server == null)
                return;
            server.playerLeaveServer(uuid);
        });

        if (player.hasPermission(Config.SKIP_QUEUE)) { //Only add non staff
            return;
        }
        ServerWrapper server = serverManager.getServer(event.getServer());
        if (server == null)
            return;
        server.addOnlinePlayer(uuid);
    }

    //TODO more
    @Subscribe
    public void onConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        ServerWrapper currentServer = serverManager.getServer(player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer() : null);
        Optional<RegisteredServer> server = event.getResult().getServer();
        if (server.isEmpty())
            return;
        ServerWrapper wrapper = serverManager.getServer(server.get());
        String serverName = wrapper.getServerInfo().getName().toLowerCase();

        // check if they are whitelisted
        if (wrapper.hasWhiteList()) {
            if ((!player.hasPermission(Config.BYPASS_WHITELIST))
                    && !player.hasPermission(Config.WHITELIST + "." + serverName)) {
                if (currentServer == null) // if they aren't on a server yet send them to lobby
                    event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverManager.getLobby()));
                else // if they are on a server keep them there
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                player.sendMessage(MiniMessage.miniMessage().deserialize(Messages.NOT_WHITELISTED, Placeholder.unparsed("server", serverName)));
                return;
            }
        }

        if (removeAllowed(player.getUniqueId())) { //Is allowed through queue
            return;
        }

        // if they can skip the queue, we don't need to worry about them
        if (player.hasPermission(Config.SKIP_QUEUE)) {
            if (currentServer != null)
                currentServer.playerLeaveServer(player.getUniqueId());
            return;
        }

        if (wrapper.hasQueue()) {
            // if they are from outside the network and their target has a queue, boot them to lobby and add them to queue
            if (currentServer == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverManager.getLobby()));
                QueueResponse response = wrapper.addQueue(player);//todo check if they really entered the server if not freak out
                String queueTypeMessage = getQueueTypeMessage(response);
                if (queueTypeMessage.isBlank())
                    return;
                player.sendMessage(MiniMessage.miniMessage().deserialize(Config.DIRECT_CONNECT_FULL
                        .replace("{server}", wrapper.getServerInfo().getName())
                        .replace("{position}", String.valueOf(wrapper.getPosition(player.getUniqueId())))
                        .replace("{queue_type_message}", queueTypeMessage))
                );
                return;
            }
//            if (wrapper.isNextInQueue(player)) {
//                return;
//            }
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
            if (response == QueueResponse.NOT_FULL) {
                if (currentServer != null)
                    currentServer.playerLeaveServer(player.getUniqueId());
                return;
            }

            if (response == QueueResponse.SKIP_QUEUE) {
                if (currentServer != null)
                    currentServer.playerLeaveServer(player.getUniqueId());
                return;
            }

            // check if they're already in queue
            if (response == QueueResponse.ALREADY_ADDED) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(Config.ALREADY_QUEUED
                        .replace("{server}", wrapper.getServerInfo().getName())
                        .replace("{position}", wrapper.getPosition(player.getUniqueId()) + "")));
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            // if they had a queue before, let them know
            if (previousQueue != null) {
                previousQueue.removeFromQueue(player.getUniqueId());
                player.sendMessage(MiniMessage.miniMessage().deserialize(Config.LEFT_QUEUE
                        .replace("{server}", wrapper.getServerInfo().getName())));
            }


            // if they're a new connection, send them to the lobby no matter what
            if (currentServer == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverManager.getLobby()));

                String queueTypeMessage = getQueueTypeMessage(response);
                if (queueTypeMessage.isBlank())
                    return;
                player.sendMessage(MiniMessage.miniMessage().deserialize(Config.DIRECT_CONNECT_FULL
                        .replace("{server}", wrapper.getServerInfo().getName())
                        .replace("{position}", String.valueOf(wrapper.getPosition(player.getUniqueId())))
                        .replace("{queue_type_message}", queueTypeMessage))
                );
                return;
            }

            // cancel the join event
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

            // tell them they were added to the queue
            String queueTypeMessage = getQueueTypeMessage(response);
            if (queueTypeMessage.isBlank())
                return;
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.JOINED_QUEUE
                    .replace("{server}", wrapper.getServerInfo().getName())
                    .replace("{position}", String.valueOf(wrapper.getPosition(player.getUniqueId())))
                    .replace("{queue_type_message}", queueTypeMessage)));
        }
    }

    private String getQueueTypeMessage(QueueResponse queueResponse) {
        switch (queueResponse) {
            case ADDED_LOW_PRIORITY -> {
                return Config.JOINED_LOW_PRIORITY;
            }
            case ADDED_MID_PRIORITY -> {
                return Config.JOINED_MID_PRIORITY;
            }
            case ADDED_HIGH_PRIORITY -> {
                return Config.JOINED_HIGH_PRIORITY;
            }
            default -> {
                return "";
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Optional<ServerConnection> currentServer = player.getCurrentServer();
        if (currentServer.isPresent()) {
            ServerWrapper server = serverManager.getServer(currentServer.get().getServer());
            if (server != null)
                server.playerLeaveServer(uuid);
        }
        ServerWrapper wrapper = serverManager.getQueuedServer(uuid);

        if (wrapper != null) {
            wrapper.playerLeaveServer(uuid);
            wrapper.removeFromQueue(uuid);
        }
    }


    private final HashSet<UUID> allowedPlayers = new HashSet<>();

    public synchronized void addAllowed(UUID uuid) {
        allowedPlayers.add(uuid);
    }

    public synchronized boolean removeAllowed(UUID uuid) {
        return allowedPlayers.remove(uuid);
    }
}
