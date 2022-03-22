package com.alttd.alttdqueue.managers;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.ServerConfig;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ServerManager
{
    private final AlttdQueue plugin;

    private static List<ServerWrapper> servers;

    public ScheduledTask queueTask;

    //private static Configuration config;

    private static boolean initialized;

    private HashMap<UUID, AtomicInteger> playerTries;

    public ServerManager(AlttdQueue plugin)
    {
        this.plugin = plugin;
    }

    public void cleanup()
    {
        // Clear all the queues for the servers
        for (ServerWrapper serverWrapper : servers.stream().filter(wrapper -> !wrapper.isLobby()).collect(Collectors.toList()))
        {
            // Check if this server has a Queue
            if (serverWrapper.hasQueue())
            {
                List<UUID> queuedPlayers = serverWrapper.getQueuedPlayers(serverWrapper.getRoom());
                // go through the queued players and remove them from the queue
                while (!queuedPlayers.isEmpty())
                {
                    serverWrapper.removeFromQueue(queuedPlayers.remove(0));
                }
            }
        }
    }

    public void initialize()
    {
        initialized = true;

        servers = new ArrayList<>();
        playerTries = new HashMap<>();

        // go through the servers and add them to the list
        for (RegisteredServer registeredServer : plugin.getProxy().getAllServers())
        {
            //plugin.getLogger().info("adding " + registeredServer.getServerInfo().getName());
            servers.add(new ServerWrapper(registeredServer, new ServerConfig(registeredServer.getServerInfo().getName())));
        }

        // periodically connect players to their desired server
        queueTask = plugin.getProxy().getScheduler().buildTask(plugin, () ->
        {
            // go through the servers that are not lobbies...
            for (ServerWrapper serverWrapper : servers.stream().filter(wrapper -> !wrapper.isLobby()).collect(Collectors.toList()))
            {
                // check if they have room and if there's an active queue...
                if (!serverWrapper.isFull() && serverWrapper.hasQueue())
                {
                    List<UUID> queuedPlayers = serverWrapper.getQueuedPlayers(serverWrapper.getRoom());
                    // go through the queued players that we have room for...
                    while (!queuedPlayers.isEmpty())
                    {
                        Optional<Player> player = plugin.getProxy().getPlayer(queuedPlayers.remove(0));

                        if (player.isPresent())
                        {
                            Player presentPlayer = player.get();
                            // and send them to that server!
                            serverWrapper.addNextInQueue(presentPlayer);

                            presentPlayer.createConnectionRequest(serverWrapper.getRegisteredServer()).connect().thenAccept(
                                    result -> {
                                        AtomicInteger tries = playerTries.getOrDefault(presentPlayer.getUniqueId(), new AtomicInteger(0));
                                        if (result != null && result.isSuccessful() || tries.get() > 3) {
                                            serverWrapper.removeNextInQueue(presentPlayer);
                                            playerTries.remove(presentPlayer.getUniqueId());
                                        }
                                        tries.incrementAndGet();
                                    }
                            );
                            presentPlayer.sendMessage(MiniMessage.get().parse(Config.CONNECT.replace("{server}", serverWrapper.getServerInfo().getName())));
                            //Lang.CONNECT.sendInfo(player,
                            //        "{server}", serverWrapper.getServerInfo().getName());
                        }
                    }
                }
            }
        }).repeat(Config.QUEUE_FREQUENCY, TimeUnit.SECONDS).schedule();
    }

    /**
     * Looks up the {@link ServerWrapper} by name. If no server exists, this method will return {@code null}.
     *
     * @param serverName the server name to look for.
     *
     * @return the server if one exists.
     */
    public ServerWrapper getServer(String serverName)
    {
        for (ServerWrapper server : servers)
        {
            if (server.getServerInfo().getName().equalsIgnoreCase(serverName))
            {
                return server;
            }
        }
        return null;
    }

    /**
     * Looks up the {@link ServerWrapper} by {@link ServerInfo}. If no server exists, this method will return
     * {@code null}. This method has the same behavior as {@link ServerManager#getServer(String)}.
     *
     * @param serverInfo the server info to look for.
     *
     * @return the server if one exists.
     */
    public ServerWrapper getServer(ServerInfo serverInfo)
    {
        return getServer(serverInfo.getName());
    }

    /**
     * Looks up the {@link ServerWrapper} by {@link RegisteredServer}. If the {@link RegisteredServer} parameter is null or no server
     * exists, this method will return {@code null}.
     *
     * @param server the server to look for.
     *
     * @return the server if one exists.
     */
    public ServerWrapper getServer(RegisteredServer server)
    {
        if (server == null)
        {
            return null;
        }
        return getServer(server.getServerInfo());
    }

    public RegisteredServer getLobby()
    {
        List<ServerWrapper> lobbies = new ArrayList<>();

        for (ServerWrapper serverWrapper : servers)
        {
            if (serverWrapper.isLobby())
            {
                lobbies.add(serverWrapper);
            }
        }

        if (lobbies.size() <= 0)
        {
            throw new IllegalStateException("No registered lobbies.");
        }

        int targetIndex = 0;

        if (Config.LOBBY_STRATEGY.equalsIgnoreCase("LOWEST"))
        {
            int lowestCount = Integer.MAX_VALUE;
            int count;
            for (int i = 0; i < lobbies.size(); i++)
            {
                count = lobbies.get(i).getRegisteredServer().getPlayersConnected().size();
                if (count < lowestCount)
                {
                    lowestCount = count;
                    targetIndex = i;
                }
            }
        }
        else
        {
            targetIndex = new Random().nextInt(lobbies.size());
        }

        return lobbies.get(targetIndex).getRegisteredServer();
    }

    /**
     * Returns the server the given player is queued in. If the player is not queued for a server, this method returns
     * null.
     *
     * @param uuid the uuid of the player to look for.
     *
     * @return the server the given player is queued in.
     */
    public ServerWrapper getQueuedServer(UUID uuid)
    {
        for (ServerWrapper serverWrapper : servers)
        {
            if (serverWrapper.getPosition(uuid) != -1)
            {
                return serverWrapper;
            }
        }
        return null;
    }

    /**
     * @param uuid the uuid of the player to look for.
     *
     * @param uuid the uuid of the player to look for.
     * @return the server the given player is queued in.
     */
    public boolean isQueued(UUID uuid) {
        for (ServerWrapper serverWrapper : servers)
        {
            if (serverWrapper.getPosition(uuid) != -1)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    public List<ServerWrapper> getServersQueue()
    {
        return Collections.unmodifiableList(servers);
    }
}
