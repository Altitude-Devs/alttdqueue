package com.alttd.alttdqueue.config;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class tomlConfig {
/*
    public static boolean debug = true;
    public static String PREFIX = "&3[&bAltiQueue&3] &f";
    // Config
    public static String LOBBY_STRATEGY = "LOWEST";
    public static Long QUEUE_FREQUENCY = 2L;
    // Permisisons
    public static String PRIORITY_QUEUE = "altiqueue.priority-queue";
    public static String SKIP_QUEUE = "altiqueue.skip-queue";
    public static String QUEUE_COMMAND = "altiqueue.queue-command";
    public static String QUEUERELOAD_COMMAND = "altiqueue.queuereload-command";
    public static String QUEUELIST_COMMAND = "altiqueue.queuelist-command";

    // Lang
    public static String NOSERVER = "&4That server does not exist!";
    public static String QUEUE_LIST = "{server} has {players} players in its queue.";
    public static String QUEUE_LISTITEM = "{id}: {player}";
    public static String ALREADY_QUEUED = "You are already in queue for &b{server}&f. You are at position &c{position}&f.";
    public static String DIRECT_CONNECT_FULL = "&b{server}&f is full. You are at position &c{position}&f in queue. Purchase a donor rank to get a prioritized queue. Type /q leave to leave the queue.";
    public static String LEFT_QUEUE = "You have left queue for &b{server}.";
    public static String JOINED_QUEUE = "You have joined the queue for &b{server}&f. You are at position &c{position}&f. Purchase a donor rank to get a prioritized queue. Type /q leave to leave the queue.";
    public static String CONNECT = "You have been connected to &b{server}&f.";
    public static String ALREADY_CONNECTED = "You are already connected to &b{server}&f.";
    public static String POSITION_UPDATE = "You are now at position &c{position}&f for &b{server}&f.";
    public static String NOT_QUEUED = "&cYou are not queued for a server.";
    public static String ONLY_PLAYERS = "&cOnly players can run that command.";
    public static String CHECK_STATUS = "You are at position &c{position}&f for &b{server}&f. Purchase a donor rank to get a prioritized queue. Type /q leave to leave the queue.";

    // Servers
    private static void init() {
        debug = getBoolean("debug", debug);
        PREFIX = getString("prefix", PREFIX);
        // Config
        LOBBY_STRATEGY = getString("lobby-strategy", LOBBY_STRATEGY);

        // Permisisons
        PRIORITY_QUEUE = getString("permission.priority-queue", PRIORITY_QUEUE);
        SKIP_QUEUE = getString("permission.skip-queue", SKIP_QUEUE);
        QUEUE_COMMAND = getString("permission.queue-command", QUEUE_COMMAND);
        QUEUERELOAD_COMMAND = getString("permission.queuereload-command", QUEUERELOAD_COMMAND);
        QUEUELIST_COMMAND = getString("permission.queuelist-command", QUEUELIST_COMMAND);

        // Lang
        NOSERVER = getString("noserver", NOSERVER);
        QUEUE_LIST = getString("queuelist", QUEUE_LIST);
        QUEUE_LISTITEM = getString("queuelistitem", QUEUE_LISTITEM);
        ALREADY_QUEUED = getString("already-queued", ALREADY_QUEUED);
        DIRECT_CONNECT_FULL = getString("direct-connect-full", DIRECT_CONNECT_FULL);
        LEFT_QUEUE = getString("left-queue", LEFT_QUEUE);
        JOINED_QUEUE = getString("joined-queue", JOINED_QUEUE);
        CONNECT = getString("connect", CONNECT);
        ALREADY_CONNECTED = getString("already-connected", ALREADY_CONNECTED);
        POSITION_UPDATE = getString("position-update", POSITION_UPDATE);
        NOT_QUEUED = getString("not-queued", NOT_QUEUED);
        ONLY_PLAYERS = getString("only-players", ONLY_PLAYERS);
        CHECK_STATUS = getString("check-status", CHECK_STATUS);

    }

    /*
     * Do not edit anything below
     *//*
    public static void reload(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try (InputStream input = tomlConfig.class.getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        config = new Toml().read(file);
        init();
    }

    private static Toml config;

    private static String getString(String path, String def) {
        //        yaml.addDefault(path, def);
        //        return yaml.getString(path, yaml.getString(path));
        config.s
        config.getString(path, def);
        if(config.add(path, def))   config.save();
        return config.getOrElse(path, config.get(path));
    }

    private static boolean getBoolean(String path, boolean def) {
        if(config.add(path, def))   config.save();
        return config.getOrElse(path, config.get(path));
    }

    private static int getInt(String path, int def) {
        if(config.add(path, def))   config.save();
        return config.getIntOrElse(path, config.getInt(path));
    }*/

}
