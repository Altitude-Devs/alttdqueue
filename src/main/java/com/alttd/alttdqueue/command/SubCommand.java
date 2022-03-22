package com.alttd.alttdqueue.command;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public abstract class SubCommand {

    private final MiniMessage miniMessage;

    public SubCommand() {
        miniMessage = MiniMessage.get();
    }

    public abstract void onCommand(CommandSource source, String[] args);

    public abstract String getName();

    public String getPermission() {
        return "permissionwhitelist.command." + getName();
    }

    public abstract List<String> getTabComplete(CommandSource source, String[] args);

    public abstract String getHelpMessage();

    protected MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
