package com.alttd.alttdqueue.config;

import java.util.regex.Pattern;

public final class ServerConfig {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");

    private final String servername;
    private final String configPath;
    private final String defaultpath;

    public ServerConfig(String serverName) {
        this.servername = serverName;
        this.configPath = "server-settings." + servername + ".";
        this.defaultpath = "server-settings.default.";
        init();
    }

    public void init() {
        Config.readConfig(ServerConfig.class, this);
        Config.saveConfig();
    }

    public static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    private static void set(String path, Object def) {
        if(Config.config.getNode(splitPath(path)).isVirtual()) {
            Config.config.getNode(splitPath(path)).setValue(def);
        }
    }

    private boolean getBoolean(String path, boolean def) {
        set(defaultpath+path, def);
        return Config.config.getNode(splitPath(configPath+path)).getBoolean(
                Config.config.getNode(splitPath(defaultpath+path)).getBoolean(def));
    }

    private double getDouble(String path, double def) {
        set(defaultpath+path, def);
        return Config.config.getNode(splitPath(configPath+path)).getDouble(
                Config.config.getNode(splitPath(defaultpath+path)).getDouble(def));
    }

    private int getInt(String path, int def) {
        set(defaultpath+path, def);
        return Config.config.getNode(splitPath(configPath+path)).getInt(
                Config.config.getNode(splitPath(defaultpath+path)).getInt(def));
    }

    private String getString(String path, String def) {
        set(defaultpath+path, def);
        return Config.config.getNode(splitPath(configPath+path)).getString(
                Config.config.getNode(splitPath(defaultpath+path)).getString(def));
    }

    public boolean isLobby = false;
    public boolean hasPriorityQueue = false;
    public int maxPlayers = 50;
    private void ServerSettings() {
        maxPlayers = getInt("maxplayer", maxPlayers);
        isLobby = getBoolean("islobby", isLobby);
        hasPriorityQueue = getBoolean("hasPriorityQueue", hasPriorityQueue);
    }
}
