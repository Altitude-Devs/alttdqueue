package com.alttd.alttdqueue.data;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.ServerConfig;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

public class ServerWrapper
{
    private RegisteredServer registeredServer;

    private int maxPlayers;

    private boolean hasPriorityQueue;

    private boolean whiteList;

    private boolean lobby;

    private LinkedList<UUID> queue;

    private LinkedList<UUID> priorityQueue;

    private LinkedList<UUID> nextInQueue;

    private ServerConfig serverConfig;

    public ServerWrapper(RegisteredServer registeredServer, ServerConfig serverConfig)
    {
        this.registeredServer = registeredServer;

        this.maxPlayers = serverConfig.maxPlayers;
        this.hasPriorityQueue = serverConfig.hasPriorityQueue;
        this.lobby = serverConfig.isLobby;
        this.whiteList = serverConfig.hasWhiteList;

        this.queue = new LinkedList<>();
        this.nextInQueue = new LinkedList<>();
        this.serverConfig = serverConfig;

        if (this.hasPriorityQueue)
        {
            this.priorityQueue = new LinkedList<>();
        }
        this.update();
    }

    /**
     * Adds the given player to the server queue. If the target server is not full this method will return {@code true} indicating that they can be safely connected to the target server.
     *
     * @param player the player trying to connect.
     *
     * @return {@code true} if there is room on the target server, otherwise, {@code false}.
     */
    public QueueResponse addQueue(Player player)
    {
        // the server isn't full and there's no queue, send em through
        if (registeredServer.getPlayersConnected().size() < maxPlayers && !hasQueue())
        {
            return QueueResponse.NOT_FULL;
        }

        // they have permission to skip queue, send em through
        if (player.hasPermission(Config.SKIP_QUEUE))
        {
            return QueueResponse.SKIP_QUEUE;
        }

        // they're already in queue
        if ((hasPriorityQueue && priorityQueue.contains(player.getUniqueId())) || queue.contains(player.getUniqueId()))
        {
            return QueueResponse.ALREADY_ADDED;
        }

        // they have the priority queue permission
        if (hasPriorityQueue() && player.hasPermission(Config.PRIORITY_QUEUE))
        {
            priorityQueue.add(player.getUniqueId());
            return QueueResponse.ADDED_PRIORITY;
        }

        // they have normal permissions
        update();
        queue.add(player.getUniqueId());
        return QueueResponse.ADDED_STANDARD;
    }

    /**
     * Returns the position that the given player is in the server's queue.
     *
     * @param uuid the uuid of the player.
     *
     * @return the position that the given player is in the server's queue.
     */
    public int getPosition(UUID uuid)
    {
        if (hasPriorityQueue && priorityQueue.contains(uuid))
        {
            return priorityQueue.indexOf(uuid) + 1;
        }
        else if (queue.contains(uuid))
        {
            return (hasPriorityQueue ? priorityQueue.size() : 0) + queue.indexOf(uuid) + 1;
        }
        else
        {
            return -1;
        }
    }

    /**
     * Removes the given player from the queue. This method will return {@code true} if they were in queue, otherwise
     * it will return {@code false}.
     *
     * @param uuid the uuid of the player to remove.
     *
     * @return {@code true} if they were in queue, {@code false} otherwise.
     */
    public boolean removeFromQueue(UUID uuid)
    {
        return (hasPriorityQueue && priorityQueue.remove(uuid)) || queue.remove(uuid);
    }

    /**
     * Check if the player is the next player to join the server, This method will return {@code true} if they are next to join
     * @param player the player to check
     * @return {@code true} if they are next in queue, {@code false} otherwise.
     */
    public boolean isNextInQueue(Player player)
    {
        if(!nextInQueue.contains(player.getUniqueId()))
            return false;
//        nextInQueue.remove(player.getUniqueId());
        return true;
    }

    public boolean removeNextInQueue(Player player)
    {
        if(!nextInQueue.contains(player.getUniqueId()))
            return false;
        nextInQueue.remove(player.getUniqueId());
        return true;
    }

    /**
     * Allow the player to join the server
     * @param player the player that can join the server
     */
    public void addNextInQueue(Player player)
    {
        if(nextInQueue.contains(player.getUniqueId())) return;
        nextInQueue.add(player.getUniqueId());
    }

    /**
     * Returns {@code true} if there are less players connected than the limit.
     *
     * @return {@code true} if there are less players connected than the limit.
     */
    public boolean isFull()
    {
        return registeredServer.getPlayersConnected().size() >= maxPlayers;
    }

    /**
     * Returns {@code true} if there is an active queue for the server.
     *
     * @return {@code true} if there is an active queue for the server.
     */
    public boolean hasQueue()
    {
        return !queue.isEmpty() || (hasPriorityQueue && !priorityQueue.isEmpty());
    }

    /**
     * Returns how many more players can connect before the server becomes full.
     *
     * @return how many more players can connect before the server becomes full.
     */
    public int getRoom()
    {
        return maxPlayers - registeredServer.getPlayersConnected().size();
    }

    public List<UUID> getNormalQueue()
    {
        return Collections.unmodifiableList(queue);
    }

    public List<UUID> getPriorityQueue()
    {
        return Collections.unmodifiableList(priorityQueue);
    }

    /**
     * Returns a list of players who are queued for the server.
     *
     * @return the players who are queued for the server.
     */
    public List<UUID> getQueuedPlayerList()
    {
        List<UUID> players = new ArrayList<>();
        if (hasPriorityQueue && priorityQueue.size() > 0)
        {
            players.addAll(getPriorityQueue());
        }
        if (queue.size() > 0)
        {
            players.addAll(getNormalQueue());
        }
        return players;
    }

    /**
     * Returns the players who are at the beginning of the queue. This removes the player from the queue, so don't
     * call it unless you want to pop UUIDs out.
     *
     * @param amount the amount of players to return.
     *
     * @return the players who are at the beginning of the queue.
     */
    public List<UUID> getQueuedPlayers(int amount)
    {
        List<UUID> players = new ArrayList<>();
        while (amount > 0)
        {
            if (hasPriorityQueue && priorityQueue.size() > 0)
            {
                try {
                    players.add(priorityQueue.remove());
                } catch (NoSuchElementException e) {
                    queueError(priorityQueue);
                }
            }
            else if (queue.size() > 0)
            {
                try {
                    players.add(queue.remove());
                } catch (NoSuchElementException e) {
                    queueError(queue);
                }
            }
            amount--;
        }
        return players;
    }

    private void queueError(LinkedList<UUID> queue)
    {
        AlttdQueue instance = AlttdQueue.getInstance();
        ProxyServer proxy = instance.getProxy();
        try {
            for (UUID uuid : queue)
                proxy.getPlayer(uuid)
                        .ifPresent(player -> player.sendMessage(MiniMessage.miniMessage()
                                .deserialize("<gold>Rejoin queue!</gold> <red>There was an error with the queue so it was reset, please join the queue again.<red>")));
        } catch (Exception ignored) {
            instance.getLogger().warn("Unable to notify players of clearing queue");
        }
        queue.clear();
    }

    /**
     * Returns the maximum number of players the server supports.
     *
     * @return the maximum number of players the server supports.
     */
    public int getMaxPlayers()
    {
        return maxPlayers;
    }

    /**
     * Returns whether or not this server has a priority queue.
     *
     * @return whether or not this server has a priority queue.
     */
    public boolean hasPriorityQueue()
    {
        return hasPriorityQueue;
    }

    /**
     * Returns the {@link RegisteredServer} for this {@link ServerWrapper}.
     *
     * @return the {@link RegisteredServer} for this {@link ServerWrapper}.
     */
    public RegisteredServer getRegisteredServer()
    {
        return registeredServer;
    }

    /**
     * Returns the {@link ServerInfo} for this {@link ServerWrapper}.
     *
     * @return the {@link ServerInfo} for this {@link ServerWrapper}.
     */
    public ServerInfo getServerInfo()
    {
        return registeredServer.getServerInfo();
    }

    /**
     * Returns whether or not this server is a lobby.
     *
     * @return whether or not this server is a lobby.
     */
    public boolean isLobby()
    {
        return lobby;
    }

    /**
     * Returns whether or not this server has a whitelist.
     *
     * @return whether or not this server has a whitelist.
     */
    public boolean hasWhiteList()
    {
        return whiteList;
    }

    public void setWhiteList(boolean val) {
        this.whiteList = val;
        ServerConfig.setAndSaveUnsafe(serverConfig.configPath + "hasWhiteList", val);
    }

    /**
     * Update the max player field if needed
     */
    public void update() {
//        CompletableFuture<ServerPing> serverPingCompletableFuture = registeredServer.ping().whenComplete((result, error) -> {
//            if (error == null) {
//                maxPlayers = result.asBuilder().getMaximumPlayers();
//                return;
//            }
//        });
        //Commented out to rely only on the config
    }

    @Override
    public String toString() {
        return getRegisteredServer().getServerInfo().getName()
                + "\nmaxplayers: " + getMaxPlayers()
                + "\nhasPriorityQueue: " + hasPriorityQueue()
                + "\nhasWhiteList: " + hasWhiteList()
                + "\nlobby: " + isLobby()
                + "\nisfull: " + isFull();
    }

}
