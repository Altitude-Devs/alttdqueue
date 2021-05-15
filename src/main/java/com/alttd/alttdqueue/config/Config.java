package com.alttd.alttdqueue.config;

import com.alttd.alttdqueue.AlttdQueue;
import com.google.common.base.Throwables;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

public final class Config {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "This is a test file.";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YAMLConfigurationLoader configLoader;

    static int version;
    static boolean verbose;

    public static void init(File path) {
        CONFIG_FILE = new File(path, "config.yml");;
        configLoader = YAMLConfigurationLoader.builder()
                .setFile(CONFIG_FILE)
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .build();
        if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.createNewFile();
            } catch (IOException error) {
                //Bukkit.getLogger().log(Level.SEVERE, "Could not create config.yml, see the error below for details.", e);
                error.printStackTrace();
            }
        }

        try {
            config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            //Bukkit.getLogger().log(Level.SEVERE, "Could not load config.yml, please correct your syntax errors.", e);
        }

        configLoader.getDefaultOptions().setHeader(HEADER);
        configLoader.getDefaultOptions().withShouldCopyDefaults(true);

        verbose = getBoolean("verbose", true);
        version = getInt("config-version", 1);

        readConfig(Config.class, null);
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
            //Bukkit.getLogger().log(Level.SEVERE, "Could not save config.yml, see the error below for details.", e);
        }
    }

    static void readConfig(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException ex) {
                        throw Throwables.propagate(ex.getCause());
                    } catch (Exception ex) {
                        //Bukkit.getLogger().log(Level.SEVERE, "Error invoking " + method, ex);
                    }
                }
            }
        }
        try {
            configLoader.save(config);
        } catch (IOException ex) {
            //Bukkit.getLogger().log(Level.SEVERE, "Could not save " + CONFIG_FILE, ex);
        }
    }

    public static boolean saveConfig() {
        try {
            configLoader.save(config);
            return true;
        } catch (IOException ex) {
            return false;
            //Bukkit.getLogger().log(Level.SEVERE, "Could not save " + CONFIG_FILE, ex);
        }
    }

    private static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    private static void set(String path, Object def) {
        if(config.getNode(splitPath(path)).isVirtual())
            config.getNode(splitPath(path)).setValue(def);
    }

    private static boolean getBoolean(String path, boolean def) {
        set(path, def);
        return config.getNode(splitPath(path)).getBoolean(def);
    }

    private static double getDouble(String path, double def) {
        set(path, def);
        return config.getNode(splitPath(path)).getDouble(def);
    }

    private static int getInt(String path, int def) {
        set(path, def);
        return config.getNode(splitPath(path)).getInt(def);
    }

    private static String getString(String path, String def) {
        set(path, def);
        return config.getNode(splitPath(path)).getString(def);
    }

    private static Long getLong(String path, Long def) {
        set(path, def);
        return config.getNode(splitPath(path)).getLong(def);
    }

    public static String LOBBY_STRATEGY = "LOWEST";
    public static Long QUEUE_FREQUENCY = 2L;
    private static void Settings() {
        LOBBY_STRATEGY = getString("lobby.strategy", LOBBY_STRATEGY);
        QUEUE_FREQUENCY = getLong("lobby.frequency", QUEUE_FREQUENCY);
    }

    public static String PRIORITY_QUEUE = "altiqueue.priority-queue";
    public static String SKIP_QUEUE = "altiqueue.skip-queue";
    public static String QUEUE_COMMAND = "altiqueue.queue-command";
    public static String QUEUERELOAD_COMMAND = "altiqueue.queuereload-command";
    private static void Permissions() {
        PRIORITY_QUEUE = getString("permission.priority-queue", PRIORITY_QUEUE);
        SKIP_QUEUE = getString("permission.skip-queue", SKIP_QUEUE);
        QUEUE_COMMAND = getString("permission.queue-command", QUEUE_COMMAND);
        QUEUERELOAD_COMMAND = getString("permission.queuereload-command", QUEUERELOAD_COMMAND);
    }

    // TODO reload message and all messages using minimsseage:(
    public static String NOSERVER = "<red>That server does not exist!";
    public static String QUEUE_LIST = "<yellow>{server} has {players} players in its queue.";
    public static String QUEUE_LISTITEM = "{id}: {player}";
    public static String ALREADY_QUEUED = "You are already in queue for <aqua>{server}</aqua>. You are at position <red>{position}</red>.";
    public static String DIRECT_CONNECT_FULL = "<aqua>{server}</aqua> is full. You are at position <red>{position}</red> in queue. Purchase a donor rank to get a prioritized queue. Type /q leave to leave the queue.";
    public static String LEFT_QUEUE = "You have left queue for <aqua>{server}</aqua>.";
    public static String JOINED_QUEUE = "You have joined the queue for <aqua>{server}</aqua>. You are at position <red>{position}</red>. Purchase a donor rank to get a prioritized queue. Type /q leave to leave the queue.";
    public static String CONNECT = "You have been connected to <aqua>{server}</aqua>.";
    public static String ALREADY_CONNECTED = "You are already connected to <aqua>{server}</aqua>.";
    public static String POSITION_UPDATE = "You are now at position <red>{position}</red> for <aqua>{server}</aqua>.";
    public static String NOT_QUEUED = "<red>You are not queued for a server.";
    public static String ONLY_PLAYERS = "<red>Only players can run that command.";
    public static String CHECK_STATUS = "You are at position <red>{position}</red> for <aqua>{server}</aqua>. Purchase a donor rank to get a prioritized queue. Type /q leave to leave the queue.";
    public static String RELOAD = "<red>AlttdQueue config reloaded.";
    private static void messages() {
        NOSERVER = getString("messages.noserver", NOSERVER);
        QUEUE_LIST = getString("messages.queuelist", QUEUE_LIST);
        QUEUE_LISTITEM = getString("messages.queuelistitem", QUEUE_LISTITEM);
        ALREADY_QUEUED = getString("messages.already-queued", ALREADY_QUEUED);
        DIRECT_CONNECT_FULL = getString("messages.direct-connect-full", DIRECT_CONNECT_FULL);
        LEFT_QUEUE = getString("messages.eft-queue", LEFT_QUEUE);
        JOINED_QUEUE = getString("messages.joined-queue", JOINED_QUEUE);
        CONNECT = getString("messages.connect", CONNECT);
        ALREADY_CONNECTED = getString("messages.already-connected", ALREADY_CONNECTED);
        POSITION_UPDATE = getString("messages.position-update", POSITION_UPDATE);
        NOT_QUEUED = getString("messages.not-queued", NOT_QUEUED);
        ONLY_PLAYERS = getString("messages.only-players", ONLY_PLAYERS);
        CHECK_STATUS = getString("messages.check-status", CHECK_STATUS);
        RELOAD = getString("messages.reload", RELOAD);
    }

}
