package com.alttd.alttdqueue.command;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.data.ServerWrapper;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Queue {

    public Queue(AlttdQueue plugin) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("queue")
                .requires(ctx -> ctx.hasPermission(Config.QUEUE_COMMAND))
                .then(LiteralArgumentBuilder
                        .<CommandSource>literal("info")
                        .then(RequiredArgumentBuilder
                                .<CommandSource, String>argument("server", StringArgumentType.string())
                                .requires(ctx -> ctx.hasPermission(Config.QUEUERELOAD_COMMAND))
                                .suggests((context, builder) -> {
                                    Collection<String> possibleValues = new ArrayList<>();
                                    for (ServerWrapper serverWrapper : plugin.getServerManager().getServersQueue()) {
                                        possibleValues.add(serverWrapper.getServerInfo().getName());
                                    }
                                    if(possibleValues.isEmpty()) return Suggestions.empty();

                                    String remaining = builder.getRemaining().toLowerCase();
                                    for (String str : possibleValues) {
                                        if (str.toLowerCase().startsWith(remaining)) {
                                            builder.suggest(str = StringArgumentType.escapeIfRequired(str));
                                        }
                                    }

                                    return builder.buildFuture();

                                })
                                .executes(context -> {
                                    String server = context.getArgument("server", String.class);
                                    ServerWrapper serverWrapper = plugin.getServerManager().getServer(server);
                                    if (serverWrapper == null) {
                                        context.getSource().sendMessage(MiniMessage.miniMessage().deserialize(Config.NOSERVER));
                                        return 1;
                                    }
                                    context.getSource().sendMessage(MiniMessage.miniMessage().deserialize(server + ":" + serverWrapper.toString()));
                                    return 1;
                                }))
                        .executes(context -> {
                            if(context.getSource() instanceof Player) {
                                Player player = (Player) context.getSource();

                                ServerWrapper serverWrapper = plugin.getServerManager().getQueuedServer(player.getUniqueId());
                                checkQueue(player, serverWrapper);
                            }
                            return 1;
                        }))
                .then(LiteralArgumentBuilder
                        .<CommandSource>literal("reload")
                        .requires(ctx -> ctx.hasPermission(Config.QUEUERELOAD_COMMAND))
                        .executes(context -> {
                            plugin.reload();
                            //context.getSource().sendMessage(Component.text("Reload successful."));
                            context.getSource().sendMessage(MiniMessage.miniMessage().deserialize(Config.RELOAD));
                            return 1;
                        }))
                .then(LiteralArgumentBuilder
                        .<CommandSource>literal("leave")
                        .executes(context -> {
                            if(context.getSource() instanceof Player) {
                                Player player = (Player) context.getSource();

                                ServerWrapper serverWrapper = plugin.getServerManager().getQueuedServer(player.getUniqueId());
                                leaveQueue(player, serverWrapper);
                            }
                            return 1;
                        }))
                .then(LiteralArgumentBuilder
                        .<CommandSource>literal("list")
                        .then(RequiredArgumentBuilder
                                .<CommandSource, String>argument("server", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    Collection<String> possibleValues = new ArrayList<>();
                                    for (ServerWrapper serverWrapper : plugin.getServerManager().getServersQueue()) {
                                        if (!serverWrapper.isLobby() && serverWrapper.hasQueue()) {
                                            possibleValues.add(serverWrapper.getServerInfo().getName());
                                        }
                                    }
                                    if(possibleValues.isEmpty()) return Suggestions.empty();
                                    String remaining = builder.getRemaining().toLowerCase();
                                    for (String str : possibleValues) {
                                        if (str.toLowerCase().startsWith(remaining)) {
                                            builder.suggest(str = StringArgumentType.escapeIfRequired(str));
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String server = context.getArgument("server", String.class);
                                    ServerWrapper serverWrapper = plugin.getServerManager().getServer(server);
                                    if (serverWrapper == null) {
                                        context.getSource().sendMessage(MiniMessage.miniMessage().deserialize(Config.NOSERVER));
                                        return 1;
                                    }
                                    List<UUID> uuids = serverWrapper.getQueuedPlayerList();

                                    context.getSource().sendMessage(MiniMessage.miniMessage().deserialize(Config.QUEUE_LIST
                                            .replace("{server}", serverWrapper.getServerInfo().getName())
                                            .replace("{players}", uuids.size() + "")));
                                    for (int i = 0; i < uuids.size() && i < 11; i++) {
                                        plugin.getProxy().getPlayer(uuids.get(i)).ifPresent(Player::getUsername);
                                        context.getSource().sendMessage(MiniMessage.miniMessage().deserialize(Config.QUEUE_LISTITEM
                                                .replace("{player}", plugin.getProxy().getPlayer(uuids.get(i)).isPresent() ? plugin.getProxy().getPlayer(uuids.get(i)).get().getUsername() : "error")
                                                .replace("{id}", i+1+"")));
                                    }
                                    return 1;
                                })))
                .executes(context -> {
                    if(context.getSource() instanceof Player) {
                        Player player = (Player) context.getSource();

                        ServerWrapper serverWrapper = plugin.getServerManager().getQueuedServer(player.getUniqueId());
                        checkQueue(player, serverWrapper);
                    }
                    return 1;
                })
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta meta = plugin.getProxy().getCommandManager().metaBuilder(brigadierCommand).build();

        plugin.getProxy().getCommandManager().register(meta, brigadierCommand);
    }

    private void checkQueue(Player player, ServerWrapper serverWrapper) {
        if (serverWrapper == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.NOT_QUEUED));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Config.CHECK_STATUS
                    .replace("{server}", serverWrapper.getServerInfo().getName())
                    .replace("{position}", serverWrapper.getPosition(player.getUniqueId()) + "")));
        }
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
