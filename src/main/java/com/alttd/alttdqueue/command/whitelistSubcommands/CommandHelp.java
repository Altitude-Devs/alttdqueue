package com.alttd.alttdqueue.command.whitelistSubcommands;

import com.alttd.alttdqueue.command.WhitelistCommandManager;
import com.alttd.alttdqueue.command.SubCommand;
import com.alttd.alttdqueue.config.Messages;
import com.velocitypowered.api.command.CommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp extends SubCommand {

    private final WhitelistCommandManager whitelistCommandManager;

    public CommandHelp(WhitelistCommandManager whitelistCommandManager) {
        super();
        this.whitelistCommandManager = whitelistCommandManager;
    }

    @Override
    public void onCommand(CommandSource source, String[] args) {
        source.sendMessage(getMiniMessage().deserialize(Messages.HELP_MESSAGE_WRAPPER.replaceAll("<commands>", whitelistCommandManager
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
        return Messages.HELP_COMMAND_HELP;
    }
}
