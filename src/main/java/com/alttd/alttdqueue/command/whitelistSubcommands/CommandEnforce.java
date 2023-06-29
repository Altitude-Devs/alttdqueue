package com.alttd.alttdqueue.command.whitelistSubcommands;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.command.SubCommand;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.alttd.alttdqueue.util.Util;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.List;

public class CommandEnforce extends SubCommand {
    @Override
    public void onCommand(CommandSource source, String[] args) {
        if (args.length != 2) {
            source.sendMessage(getMiniMessage().deserialize(getHelpMessage()));
            return;
        }

        String serverName = args[1];
        ServerWrapper wrapper = AlttdQueue.getInstance().getServerManager().getServer(serverName);
        if (wrapper == null) {
            source.sendMessage(getMiniMessage().deserialize(Config.NOSERVER, Placeholder.unparsed("server", serverName)));
            return;
        }
        if (!wrapper.hasWhiteList()) {
            source.sendMessage(getMiniMessage().deserialize(Messages.WHITELIST_OFF, Placeholder.unparsed("server", serverName)));
            return;
        }

        Component kickMessage = getMiniMessage().deserialize(Messages.NOT_WHITELISTED, Placeholder.unparsed("server", serverName));

        Util.enforceWhitelistForServer(serverName, wrapper.getRegisteredServer(), kickMessage);

        source.sendMessage(getMiniMessage().deserialize(Messages.ENFORCED_WHITELIST, Placeholder.unparsed("server", serverName)));
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
        return Messages.PW_HELP_COMMAND_ENFORCE;
    }
}
