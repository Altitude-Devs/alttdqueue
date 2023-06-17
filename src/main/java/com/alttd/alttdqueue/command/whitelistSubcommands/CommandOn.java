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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

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
            source.sendMessage(getMiniMessage().deserialize(Messages.INVALID_SERVER, Placeholder.unparsed("server", serverName)));
            return;
        }

        ServerWrapper wrapper = AlttdQueue.getInstance().getServerManager().getServer(serverName);
        if (wrapper == null) {
            source.sendMessage(getMiniMessage().deserialize(Config.NOSERVER, Placeholder.unparsed("server", serverName)));
            return;
        }
        if (wrapper.hasWhiteList()) {
            source.sendMessage(getMiniMessage().deserialize(Messages.ALREADY_ON, Placeholder.unparsed("server", serverName)));
            return;
        }
        wrapper.setWhiteList(true);

        Component kickMessage = getMiniMessage().deserialize(Messages.NOT_WHITELISTED, Placeholder.unparsed("server", serverName));

        Util.enforceWhitelistForServer(serverName, optionalRegisteredServer.get(), kickMessage);

        source.sendMessage(getMiniMessage().deserialize(Messages.TURNED_ON, Placeholder.unparsed("server", serverName)));
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
        return Messages.PW_HELP_COMMAND_ON;
    }
}
