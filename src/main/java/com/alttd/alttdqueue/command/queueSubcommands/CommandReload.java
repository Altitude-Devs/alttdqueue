package com.alttd.alttdqueue.command.queueSubcommands;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.command.SubCommand;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.Messages;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class CommandReload extends SubCommand {

    private final AlttdQueue plugin;

    public CommandReload(AlttdQueue plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSource source, String[] args) {
        plugin.reload();
        source.sendMessage(MiniMessage.miniMessage().deserialize(Config.RELOAD));
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public List<String> getTabComplete(CommandSource source, String[] args) {
        return List.of();
    }

    @Override
    public String getHelpMessage() {
        return Messages.Q_HELP_COMMAND_RELOAD;
    }
}
