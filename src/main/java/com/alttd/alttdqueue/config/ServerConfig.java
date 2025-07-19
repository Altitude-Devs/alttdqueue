package com.alttd.alttdqueue.config;

import com.alttd.alttdqueue.AlttdQueue;
import com.alttd.alttdqueue.data.Priority;
import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ServerConfig {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");

    private final String servername;
    public final String configPath;
    private final String defaultpath;
    private final Logger logger;

    public ServerConfig(String serverName) {
        this.servername = serverName;
        this.logger = AlttdQueue.getInstance().getLogger();
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

    public static void setAndSave(String path, Object def) {
        set(path, def);
        Config.saveConfig();
    }

    public static void setAndSaveUnsafe(String path, Object def) {
        setUnsafe(path, def);
        Config.saveConfig();
    }

    private static void set(String path, Object def) {
        if(Config.config.node(splitPath(path)).virtual()) {
            try {
                Config.config.node(splitPath(path)).set(def);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void setUnsafe(String path, Object def) {
        try {
            Config.config.node(splitPath(path)).set(def);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getBoolean(String path, boolean def) {
        set(defaultpath + path, def);
        return Config.config.node(splitPath(configPath + path)).getBoolean(
                Config.config.node(splitPath(defaultpath + path)).getBoolean(def));
    }

    private double getDouble(String path, double def) {
        set(defaultpath + path, def);
        return Config.config.node(splitPath(configPath + path)).getDouble(
                Config.config.node(splitPath(defaultpath + path)).getDouble(def));
    }

    private int getInt(String path, int def) {
        set(defaultpath + path, def);
        return Config.config.node(splitPath(configPath + path)).getInt(
                Config.config.node(splitPath(defaultpath + path)).getInt(def));
    }

    private String getString(String path, String def) {
        set(defaultpath + path, def);
        return Config.config.node(splitPath(configPath + path)).getString(
                Config.config.node(splitPath(defaultpath + path)).getString(def));
    }

    private List<String> getStringList(String path, List<String> def) {
        set(defaultpath + path, def);
        try {
            return Config.config.node(splitPath(configPath + path)).
                    getList(String.class,
                            Config.config.node(splitPath(defaultpath + path)).getList(String.class, def));
        } catch (Exception e) {
            logger.warn("Unable to load string list at path: [" + path + "] return default value");
            return def;
        }
    }

    public boolean isLobby = false;
    public Priority[] priorityOrder = {Priority.HIGH, Priority.MID, Priority.MID, Priority.LOW};
    public int maxPlayers = 50;
    public boolean hasWhiteList = false;

    private void ServerSettings() {
        maxPlayers = getInt("maxplayer", maxPlayers);
        isLobby = getBoolean("islobby", isLobby);
        priorityOrder = getStringList("priorityOrder",
                                      Arrays.stream(priorityOrder).map(Priority::toString).collect(Collectors.toList()))
                .stream().map(Priority::valueOf).toList().toArray(new Priority[0]);
        hasWhiteList = getBoolean("hasWhiteList", hasWhiteList);
    }
}
