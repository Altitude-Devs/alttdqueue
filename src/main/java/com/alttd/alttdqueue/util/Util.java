package com.alttd.alttdqueue.util;

import com.alttd.alttdqueue.AlttdQueue;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

public class Util {

    public static void enforceWhitelistForServer(String serverName, RegisteredServer server, Component kickMessage) {
        RegisteredServer lobby = null;
        try {
            lobby = AlttdQueue.getInstance().getServerManager().getLobby();
        } catch (IllegalStateException ignored) {
        }

        RegisteredServer finalLobby = lobby;
        server.getPlayersConnected().stream()
                .filter(player -> !player.hasPermission("permissionwhitelist.join.all") && !player.hasPermission("permissionwhitelist.join." + serverName))
                .forEach(player -> {
                    if (finalLobby != null)
                        player.createConnectionRequest(finalLobby);
                    else
                        player.disconnect(kickMessage);
                    player.sendMessage(kickMessage);
                });
    }
}
