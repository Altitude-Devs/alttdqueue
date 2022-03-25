package com.alttd.alttdqueue.util;

import com.alttd.alttdqueue.AlttdQueue;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

public class Util {

    public static void enforceWhitelistForServer(String serverName, RegisteredServer server, Component kickMessage) {
        RegisteredServer lobby = AlttdQueue.getInstance().getServerManager().getLobby();

//        if (optionalDefaultServer.isEmpty() || optionalDefaultServer.get().equals(server)) {
//            AlttdQueue.getInstance().getLogger().warn("Unable to find default server or enforcing whitelist on default server, kicking all players without permission to be here.");
//            server.getPlayersConnected().stream()
//                    .filter(player -> !player.hasPermission("permissionwhitelist.join.all") && !player.hasPermission("permissionwhitelist.join." + serverName))
//                    .forEach(player -> player.disconnect(kickMessage));
//            return;
//        }

        server.getPlayersConnected().stream()
                .filter(player -> !player.hasPermission("permissionwhitelist.join.all") && !player.hasPermission("permissionwhitelist.join." + serverName))
                .forEach(player -> {
                    player.createConnectionRequest(lobby);
                    player.sendMessage(kickMessage);
                });
    }
}
