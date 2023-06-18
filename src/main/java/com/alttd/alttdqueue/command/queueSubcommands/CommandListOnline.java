package com.alttd.alttdqueue.command.queueSubcommands;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.command.SubCommand;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;
import java.util.stream.Collectors;

public class CommandListOnline extends SubCommand {

    private final AlttdQueue plugin;

    public CommandListOnline(AlttdQueue plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSource source, String[] args) {
        if (args.length < 2) {
            source.sendMessage(getMiniMessage().deserialize(getHelpMessage()));
            return;
        }
        ServerWrapper server = plugin.getServerManager().getServer(args[1]);
        if (server == null) {
            source.sendMessage(getMiniMessage().deserialize(Messages.INVALID_SERVER, Placeholder.parsed("server", args[1])));
            return;
        }
        HashSet<UUID> invalidOnline = new HashSet<>();
        String onlinePlayerList = server.getOnlinePlayers().stream().map(uuid -> {
                    Optional<Player> optionalPlayer = plugin.getProxy().getPlayer(uuid);
                    if (optionalPlayer.isEmpty()) {
                        invalidOnline.add(uuid);
                        return null;
                    }
                    return optionalPlayer.get();
                })
                .filter(Objects::nonNull)
                .filter(player -> {
                    Optional<ServerConnection> currentServer = player.getCurrentServer();
                    if (currentServer.isEmpty()) {
                        invalidOnline.add(player.getUniqueId());
                        return false;
                    }
                    if (!currentServer.get().getServer().equals(server.getRegisteredServer())) {
                        invalidOnline.add(player.getUniqueId());
                        return false;
                    }
                    return true;
                })
                .map(Player::getUsername)
                .collect(Collectors.joining(", "));
        String invalidPlayerList = invalidOnline.stream().map(UUID::toString).collect(Collectors.joining(", "));
        Component message = getMiniMessage().deserialize(Messages.LIST_ONLINE, TagResolver.resolver(
                Placeholder.parsed("amount", String.valueOf(server.getOnlinePlayers().size())),
                Placeholder.parsed("server", server.getServerInfo().getName()),
                Placeholder.parsed("online_players", onlinePlayerList)));
        if (!invalidOnline.isEmpty()) {
            message = message.append(getMiniMessage().deserialize(Messages.LIST_ONLINE_INVALID, TagResolver.resolver(
                    Placeholder.parsed("amount", String.valueOf(invalidOnline.size())),
                    Placeholder.parsed("invalid_players", invalidPlayerList))));
            for (UUID uuid : invalidOnline) {
                server.playerLeaveServer(uuid);
            }
        }
        source.sendMessage(message);
    }

    @Override
    public String getName() {
        return "listonline";
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
        return Messages.Q_HELP_COMMAND_LIST_ONLINE;
    }
}
