package com.alttd.alttdqueue.command.queueSubcommands;

import com.alttd.alttdqueue.command.QueueCommandManager;
import com.alttd.alttdqueue.command.SubCommand;
import com.alttd.alttdqueue.command.WhitelistCommandManager;
import com.alttd.alttdqueue.config.Messages;
import com.velocitypowered.api.command.CommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp extends SubCommand {

    private final QueueCommandManager queueCommandManager;

    public CommandHelp(QueueCommandManager queueCommandManager) {
        this.queueCommandManager = queueCommandManager;
    }

    @Override
    public void onCommand(CommandSource source, String[] args) {
        source.sendMessage(getMiniMessage().deserialize(Messages.Q_HELP_MESSAGE_WRAPPER.replaceAll("<commands>", queueCommandManager
                .getSubCommands().stream()
                .filter(subCommand -> source.hasPermission(subCommand.getPermission()))
                .map(SubCommand::getHelpMessage)
                .collect(Collectors.joining("\n")))));
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getTabComplete(CommandSource source, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Messages.Q_HELP_COMMAND_HELP;
    }
}
