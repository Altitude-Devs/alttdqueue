package com.alttd.alttdqueue.command.whitelistSubcommands;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.command.SubCommand;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.alttd.alttdqueue.util.Util;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandOn extends SubCommand {
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

        ServerWrapper wrapper = AlttdQueue.getInstance().getServerManager().getServer(serverName);
        if (wrapper == null) {
            source.sendMessage(getMiniMessage().parse(Config.NOSERVER, Template.of("server", serverName)));
            return;
        }
        if (wrapper.hasWhiteList()) {
            source.sendMessage(getMiniMessage().parse(Messages.ALREADY_ON, Template.of("server", serverName)));
            return;
        }
        wrapper.setWhiteList(true);

        Component kickMessage = getMiniMessage().parse(Messages.NOT_WHITELISTED, Template.of("server", serverName));

        Util.enforceWhitelistForServer(serverName, optionalRegisteredServer.get(), kickMessage);

        source.sendMessage(getMiniMessage().parse(Messages.TURNED_ON, Template.of("server", serverName)));
    }

    @Override
    public String getName() {
        return "on";
    }

    @Override
    public List<String> getTabComplete(CommandSource source, String[] args) {
        if (args.length <= 1)
            return AlttdQueue.getInstance().getProxy().getAllServers().stream()
                .map(registeredServer -> registeredServer.getServerInfo().getName())
                .collect(Collectors.toList());
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Messages.HELP_COMMAND_ON;
    }
}
