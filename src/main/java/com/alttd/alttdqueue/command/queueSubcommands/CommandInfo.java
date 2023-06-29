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

public class CommandInfo extends SubCommand {

    private final AlttdQueue plugin;

    public CommandInfo(AlttdQueue plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSource source, String[] args) {
        if (args.length == 1)
            playerQueueInfo(source);
        else if (args.length == 2)
            serverQueueInfo(source, args[1]);
        else
            source.sendMessage(getMiniMessage().deserialize(getHelpMessage()));
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public List<String> getTabComplete(CommandSource source, String[] args) {
        if (args.length != 2 || !source.hasPermission(getPermission() + ".servers")) {
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
        return Messages.Q_HELP_COMMAND_INFO;
    }

    private void playerQueueInfo(CommandSource source) {
        if(!(source instanceof Player player)) {
            source.sendMessage(getMiniMessage().deserialize(Messages.NO_CONSOLE));
            return;
        }
        ServerWrapper serverWrapper = plugin.getServerManager().getQueuedServer(player.getUniqueId());
        QueueUtil.checkQueue(player, serverWrapper);
    }

    private void serverQueueInfo(CommandSource source, String server) {
        if (!source.hasPermission(getPermission() + ".servers")) {
            source.sendMessage(getMiniMessage().deserialize(Messages.NO_PERMISSION));
            return;
        }

        ServerWrapper serverWrapper = plugin.getServerManager().getServer(server);
        if (serverWrapper == null) {
            source.sendMessage(MiniMessage.miniMessage().deserialize(Config.NOSERVER));
            return;
        }
        source.sendMessage(MiniMessage.miniMessage().deserialize(server + ":" + serverWrapper));
    }
}
