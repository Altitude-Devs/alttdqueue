package com.alttd.alttdqueue.data;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.ServerConfig;
import com.alttd.alttdqueue.listeners.ConnectionListener;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.stream.Collectors;

public class ServerWrapper {

    private RegisteredServer registeredServer;

    private int maxPlayers;

    private boolean whiteList;

    private boolean lobby;

    private final Priority[] priorityOrder;
    private final HashSet<UUID> onlinePlayers;
    private final LinkedList<QueuePlayer> queue;
    private final LinkedList<QueuePlayer> highPriorityQueue;
    private final LinkedList<QueuePlayer> midPriorityQueue;
    private final LinkedList<QueuePlayer> lowPriorityQueue;
    private int posInPriority;

    private ServerConfig serverConfig;
    private final AlttdQueue plugin;
    private final ConnectionListener connectionListener;

    public ServerWrapper(RegisteredServer registeredServer, ServerConfig serverConfig, AlttdQueue plugin, ConnectionListener connectionListener) {
        this.registeredServer = registeredServer;

        this.maxPlayers = serverConfig.maxPlayers;
        this.priorityOrder = serverConfig.priorityOrder;
        this.lobby = serverConfig.isLobby;
        this.whiteList = serverConfig.hasWhiteList;
        this.plugin = plugin;

        this.queue = new LinkedList<>();
        this.highPriorityQueue = new LinkedList<>();
        this.midPriorityQueue = new LinkedList<>();
        this.lowPriorityQueue = new LinkedList<>();
        this.serverConfig = serverConfig;
        this.connectionListener = connectionListener;
        onlinePlayers = new HashSet<>();
        updateOnlinePlayers();
    }

    public synchronized void addOnlinePlayer(UUID uuid) {
        onlinePlayers.add(uuid);
    }

    public synchronized void playerLeaveServer(UUID uuid) {
        onlinePlayers.remove(uuid);
        if (isFull())
            return;
        allowNextPlayerThrough();
    }

    private void allowNextPlayerThrough() {
        if (!hasQueue() || isFull())
            return;
        if (posInPriority < priorityOrder.length - 1) { //Increase the spot of the current priority since we're letting one player join
            posInPriority++;
        } else {
            posInPriority = 0;
        }
        QueuePlayer queuePlayer = queue.remove(0);
        removeFromPriorityQueues(queuePlayer);
        Optional<Player> optionalPlayer = plugin.getProxy().getPlayer(queuePlayer.uuid());
        if (optionalPlayer.isEmpty()) {
            plugin.getLogger().warn("Had offline player in queue.");
            allowNextPlayerThrough();
            return;
        }

        Player player = optionalPlayer.get();
        //Running this in a new thread so it can properly use #removeOnlinePlayerDueToError if needed and so the server wrapper isn't waiting on the player to connect
        new Thread(new PlayerConnectToServerTask(queuePlayer, player, this, plugin, connectionListener)).start();
    }

    /**
     * Adds the given player to the server queue. If the target server is not full this method will return {@code true} indicating that they can be safely connected to the target server.
     *
     * @param player the player trying to connect.
     *
     * @return {@code true} if there is room on the target server, otherwise, {@code false}.
     */
    public synchronized QueueResponse addQueue(Player player)
    {
        // they have permission to skip queue, send em through
        if (player.hasPermission(Config.SKIP_QUEUE)) {
            return QueueResponse.SKIP_QUEUE;
        }

        // the server isn't full and there's no queue, send em through
        if (!isFull() && !hasQueue()) {
            return QueueResponse.NOT_FULL;
        }

        UUID uuid = player.getUniqueId();

        // they're already in queue
        if (isInQueue(uuid)) {
            return QueueResponse.ALREADY_ADDED;
        }

        // add them to the correct queue for their permission
        if (player.hasPermission(Config.PERM_QUEUE_HIGH)) {
            highPriorityQueue.add(new QueuePlayer(uuid, System.currentTimeMillis(), Priority.HIGH));
            updateQueueOrder();
            return QueueResponse.ADDED_HIGH_PRIORITY;
        } else if (player.hasPermission(Config.PERM_QUEUE_MID)) {
            midPriorityQueue.add(new QueuePlayer(uuid, System.currentTimeMillis(), Priority.MID));
            updateQueueOrder();
            return QueueResponse.ADDED_MID_PRIORITY;
        } else {
            lowPriorityQueue.add(new QueuePlayer(uuid, System.currentTimeMillis(), Priority.LOW));
            updateQueueOrder();
            return QueueResponse.ADDED_LOW_PRIORITY;
        }

    }

    /**
     * Returns the position that the given player is in the server's queue.
     *
     * @param uuid the uuid of the player.
     *
     * @return the position that the given player is in the server's queue. (starts at 1)
     */
    public int getPosition(UUID uuid)
    {
        int pos = 0;
        for (QueuePlayer queuePlayer : queue) {
            if (queuePlayer.uuid().equals(uuid))
                return pos + 1;
            pos++;
        }
        return -1;
    }

    /**
     * Checks if the given player is in the queue. This method will return {@code true} if they were in queue, otherwise
     * it will return {@code false}.
     *
     * @param uuid the uuid of the player to check.
     * @return {@code true} if they were in queue, {@code false} otherwise.
     */
    public boolean isInQueue(UUID uuid) {
        return queue.stream().anyMatch(queuePlayer -> queuePlayer.uuid().equals(uuid));
    }

    /**
     * Removes the given player from the queue. This method will return {@code true} if they were in queue, otherwise
     * it will return {@code false}.
     *
     * @param uuid the uuid of the player to remove.
     * @return {@code true} if they were in queue, {@code false} otherwise.
     */
    public synchronized boolean removeFromQueue(UUID uuid) {
        Optional<QueuePlayer> any = queue.stream().filter(queuePlayer -> queuePlayer.uuid().equals(uuid)).findAny();
        if (any.isEmpty())
            return false;
        QueuePlayer queuePlayer = any.get();
        queue.remove(queuePlayer);
        removeFromPriorityQueues(queuePlayer);
        return true;
    }

    private void removeFromPriorityQueues(QueuePlayer queuePlayer) {
        highPriorityQueue.remove(queuePlayer);
        midPriorityQueue.remove(queuePlayer);
        lowPriorityQueue.remove(queuePlayer);
    }

    /**
     * Returns {@code true} if there are less players connected than the limit.
     *
     * @return {@code true} if there are less players connected than the limit.
     */
    public boolean isFull() {
        return onlinePlayers.size() >= maxPlayers;
    }

    /**
     * Returns {@code true} if there is an active queue for the server.
     *
     * @return {@code true} if there is an active queue for the server.
     */
    public boolean hasQueue() {
        return !queue.isEmpty();
    }

    public synchronized void updateOnlinePlayers() {
        onlinePlayers.clear();
        onlinePlayers.addAll(registeredServer.getPlayersConnected().stream()
                .filter(player -> !player.hasPermission(Config.SKIP_QUEUE))
                .map(Player::getUniqueId)
                .collect(Collectors.toSet())
        );
    }

    public List<UUID> getQueuedPlayerList() {
        return queue.stream().map(QueuePlayer::uuid).collect(Collectors.toList());
    }

    private synchronized void updateQueueOrder() {
        int size = highPriorityQueue.size() + midPriorityQueue.size() + lowPriorityQueue.size();
        queue.clear();
        QueueWithPos highQueue = new QueueWithPos(highPriorityQueue);
        QueueWithPos midQueue = new QueueWithPos(midPriorityQueue);
        QueueWithPos lowQueue = new QueueWithPos(lowPriorityQueue);
        int priorityPos = posInPriority;
        int playersAddedToQueue = 0;
        while (playersAddedToQueue < size) {
            playersAddedToQueue += placeNextPlayerInQueue(priorityOrder[priorityPos], highQueue, midQueue, lowQueue);
            if (priorityPos < priorityOrder.length - 1) {
                priorityPos++;
            } else {
                priorityPos = 0;
            }
        }
    }

    /**
     * Adds one player in queue based on their priority or none if there are no players for that priority in queue
     *
     * @param priority  Priority we're trying to add a player for
     * @param highQueue High priority queue with its current position
     * @param midQueue  Mid priority queue with its current position
     * @param lowQueue  Low priority queue with its current position
     * @return the amount of players added to the queue (1 or 0)
     */
    private int placeNextPlayerInQueue(Priority priority, QueueWithPos highQueue, QueueWithPos midQueue, QueueWithPos lowQueue) {
        switch (priority) {
            case HIGH -> {
                QueuePlayer nextPlayer = highQueue.getNextPlayer();
                if (nextPlayer == null) {
                    return 0;
                }

                queue.add(nextPlayer);
                return 1;
            }
            case MID -> { //MID should let people from HIGH in too
                QueuePlayer highPlayer = highQueue.peekNextPlayer();
                if (highPlayer == null) {
                    QueuePlayer nextPlayer = midQueue.getNextPlayer();
                    if (nextPlayer == null)
                        return 0;

                    queue.add(nextPlayer);
                    return 1;
                }
                QueuePlayer midPlayer = midQueue.peekNextPlayer();
                if (midPlayer == null) {
                    queue.add(highQueue.getNextPlayer());
                    return 1;
                }
                QueuePlayer nextPlayer = highPlayer.queueJoinTime() < midPlayer.queueJoinTime() ? highQueue.getNextPlayer() : midQueue.getNextPlayer();
                queue.add(nextPlayer);
                return 1;
            }
            case LOW -> {
                QueuePlayer nextPlayer = lowQueue.getNextPlayer();
                if (nextPlayer == null) {
                    return 0;
                }

                queue.add(nextPlayer);
                return 1;
            }
            default -> {
                return 0;
            }
        }
    }

    private void queueError(LinkedList<UUID> queue) {
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
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Returns whether or not this server has a priority queue.
     *
     * @return whether or not this server has a priority queue.
     */
    public Priority[] getPriorityOrder() {
        return priorityOrder;
    }

    /**
     * Returns the {@link RegisteredServer} for this {@link ServerWrapper}.
     *
     * @return the {@link RegisteredServer} for this {@link ServerWrapper}.
     */
    public RegisteredServer getRegisteredServer() {
        return registeredServer;
    }

    /**
     * Returns the {@link ServerInfo} for this {@link ServerWrapper}.
     *
     * @return the {@link ServerInfo} for this {@link ServerWrapper}.
     */
    public ServerInfo getServerInfo() {
        return registeredServer.getServerInfo();
    }

    /**
     * Returns whether or not this server is a lobby.
     *
     * @return whether or not this server is a lobby.
     */
    public boolean isLobby() {
        return lobby;
    }

    /**
     * Returns whether or not this server has a whitelist.
     *
     * @return whether or not this server has a whitelist.
     */
    public boolean hasWhiteList() {
        return whiteList;
    }

    public void setWhiteList(boolean val) {
        this.whiteList = val;
        ServerConfig.setAndSaveUnsafe(serverConfig.configPath + "hasWhiteList", val);
    }

    public void clear() {
        highPriorityQueue.clear();
        midPriorityQueue.clear();
        lowPriorityQueue.clear();
        queue.clear();
        //TODO check if this can just be clearing the list instead of updating the online players
        updateOnlinePlayers();
    }

    @Override
    public String toString() {
        return getRegisteredServer().getServerInfo().getName()
                + "\nmaxplayers: " + getMaxPlayers()
                + "\nhasWhiteList: " + hasWhiteList()
                + "\npriorityOrder: " + Arrays.stream(getPriorityOrder()).map(Priority::toString).collect(Collectors.joining(", "))
                + "\nlobby: " + isLobby()
                + "\nisfull: " + isFull();
    }

    public HashSet<UUID> getOnlinePlayers() {
        return onlinePlayers;
    }

    public Priority getPriority(UUID uuid) {
        if (highPriorityQueue.stream().anyMatch(queuePlayer -> queuePlayer.uuid().equals(uuid))) {
            return Priority.HIGH;
        }
        if (midPriorityQueue.stream().anyMatch(queuePlayer -> queuePlayer.uuid().equals(uuid))) {
            return Priority.MID;
        }
        if (lowPriorityQueue.stream().anyMatch(queuePlayer -> queuePlayer.uuid().equals(uuid))) {
            return Priority.LOW;
        }
        return null;
    }
}
