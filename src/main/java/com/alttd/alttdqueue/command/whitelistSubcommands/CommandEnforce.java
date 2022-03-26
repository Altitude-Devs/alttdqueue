package com.alttd.alttdqueue.command.whitelistSubcommands;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.command.SubCommand;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.util.Util;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandEnforce extends SubCommand {
    @Override
    public void onCommand(CommandSource source, String[] args) {
        if (args.length != 2) {
            source.sendMessage(getMiniMessage().deserialize(getHelpMessage()));
            return;
        }

        String serverName = args[1];
        Optional<RegisteredServer> optionalRegisteredServer = AlttdQueue.getInstance().getProxy().getServer(serverName);

        if (optionalRegisteredServer.isEmpty()) {
            source.sendMessage(getMiniMessage().parse(Messages.INVALID_SERVER, Template.of("server", serverName)));
            return;
        }

        RegisteredServer server = optionalRegisteredServer.get();
        Component kickMessage = getMiniMessage().parse(Messages.NOT_WHITELISTED, Template.of("server", serverName));

        Util.enforceWhitelistForServer(serverName, server, kickMessage);

        source.sendMessage(getMiniMessage().parse(Messages.ENFORCED_WHITELIST, Template.of("server", serverName)));
    }

    @Override
    public String getName() {
        return "enforce";
    }

    @Override
    public List<String> getTabComplete(CommandSource source, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Messages.HELP_COMMAND_ENFORCE;
    }
}
