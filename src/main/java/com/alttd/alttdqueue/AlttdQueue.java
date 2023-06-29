package com.alttd.alttdqueue;

import com.alttd.alttdqueue.command.QueueCommandManager;
import com.alttd.alttdqueue.command.WhitelistCommandManager;
import com.alttd.alttdqueue.config.Config;
import com.alttd.alttdqueue.config.Messages;
import com.alttd.alttdqueue.listeners.ConnectionListener;
import com.alttd.alttdqueue.managers.ServerManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Plugin(id = "alttdqueue", name = "AlttdQueue", version = "1.0.0",
        description = "A simple queue plugin for the altitude minecraft server",
        authors = "Destro174")
public class AlttdQueue {

    private static AlttdQueue plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ServerManager serverManager;

    @Inject
    AlttdQueue(final ProxyServer server, final Logger logger, @DataDirectory Path dataDirectory) {
        this.plugin = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Config.init(getDataDirectory());
        Messages.init(getDataDirectory());
        ConnectionListener connectionListener = new ConnectionListener(this);
        serverManager = new ServerManager(plugin, connectionListener);
        serverManager.initialize();
        connectionListener.init(serverManager);
        server.getEventManager().register(this, connectionListener);
        //server.getCommandManager().register(new queueCommand(this), "queue");
        server.getCommandManager().register("permissionwhitelist", new WhitelistCommandManager());
        server.getCommandManager().register("queue", new QueueCommandManager(this));
    }

    public ProxyServer getProxy() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataDirectory() {
        return dataDirectory.toFile();
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public void reload() {
        getServerManager().queueTask.cancel();
        Config.init(getDataDirectory());
        Messages.init(getDataDirectory());
        serverManager.cleanup();
        serverManager.initialize();
    }

    public static AlttdQueue getInstance() {
        return plugin;
    }
}
