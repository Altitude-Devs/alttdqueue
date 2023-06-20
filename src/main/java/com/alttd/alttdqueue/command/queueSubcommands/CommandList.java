package com.alttd.alttdqueue.command.queueSubcommands;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.command.SubCommand;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandList extends SubCommand {

    private final AlttdQueue plugin;

    public CommandList(AlttdQueue plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSource source, String[] args) {
        if (args.length == 1)
            playerQueueList(source);
        else if (args.length == 2)
            serverQueueList(source, args[1]);
        else
            source.sendMessage(getMiniMessage().deserialize(getHelpMessage()));
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public List<String> getTabComplete(CommandSource source, String[] args) {
        if (args.length != 2) {
            return List.of();
        }
        List<String> possibleValues = new ArrayList<>();
        for (ServerWrapper serverWrapper : plugin.getServerManager().getServersQueue()) {
            possibleValues.add(serverWrapper.getServerInfo().getName());
        }
        return possibleValues;
    }

    @Override
    public String getHelpMessage() {
        return Messages.Q_HELP_COMMAND_LIST;
    }

    private void playerQueueList(CommandSource source) {
        if(!(source instanceof Player player)) {
            source.sendMessage(getMiniMessage().deserialize(Messages.NO_CONSOLE));
            return;
        }

        ServerWrapper serverWrapper = plugin.getServerManager().getQueuedServer(player.getUniqueId());
        QueueUtil.checkQueue(player, serverWrapper);
    }

    private void serverQueueList(CommandSource source, String server) {
        ServerWrapper serverWrapper = plugin.getServerManager().getServer(server);
        if (serverWrapper == null) {
            source.sendMessage(MiniMessage.miniMessage().deserialize(Config.NOSERVER));
            return;
        }
        List<UUID> uuids = serverWrapper.getQueuedPlayerList();

        source.sendMessage(MiniMessage.miniMessage().deserialize(Config.QUEUE_LIST
                .replace("{server}", serverWrapper.getServerInfo().getName())
                .replace("{players}", String.valueOf(uuids.size()))));
        for (int i = 0; i < uuids.size() && i < 11; i++) {
            Optional<Player> optionalPlayer = plugin.getProxy().getPlayer(uuids.get(i));
            source.sendMessage(MiniMessage.miniMessage().deserialize(Config.QUEUE_LISTITEM
                    .replace("{player}", optionalPlayer.map(Player::getUsername).orElse("error"))
                    .replace("{id}", String.valueOf(i+1))
                    .replace("{time}", optionalPlayer.map(player -> formatTime(serverWrapper.getQueueJoinTime(player.getUniqueId()))).orElse("invalid"))));
        }
    }

    private String formatTime(long queueJoinTime) {
        if (queueJoinTime == -1)
            return "invalid";
        long timeInQueue = System.currentTimeMillis() - queueJoinTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInQueue);
        long hours = minutes / 60;
        minutes = minutes % 60;
        return hours + ":" + minutes;
    }
}
