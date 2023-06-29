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

public class CommandLeave extends SubCommand {

    private final AlttdQueue plugin;

    public CommandLeave(AlttdQueue plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSource source, String[] args) {
        if(!(source instanceof Player player)) {
            source.sendMessage(getMiniMessage().deserialize(Messages.NO_CONSOLE));
            return;
        }
        ServerWrapper serverWrapper = plugin.getServerManager().getQueuedServer(player.getUniqueId());
        leaveQueue(player, serverWrapper);
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public List<String> getTabComplete(CommandSource source, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Messages.Q_HELP_COMMAND_LEAVE;
    }

    private void leaveQueue(Player player, ServerWrapper serverWrapper) {
        if (serverWrapper == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.NOT_QUEUED));
        } else {
            serverWrapper.removeFromQueue(player.getUniqueId());
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.LEFT_QUEUE
                    .replace("{server}", serverWrapper.getServerInfo().getName())
                    .replace("{position}", serverWrapper.getPosition(player.getUniqueId()) + "")));
        }
    }
}
