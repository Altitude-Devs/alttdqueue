package com.alttd.alttdqueue.config;

import com.google.common.base.Throwables;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

public final class Messages {

    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "Messages";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YamlConfigurationLoader configLoader;

    static int version;
    static boolean verbose;

    public static void init(File path) {
        CONFIG_FILE = new File(path, "messages.yml");;
        configLoader = YamlConfigurationLoader.builder()
                .file(CONFIG_FILE)
                .nodeStyle(NodeStyle.BLOCK)
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

        configLoader.defaultOptions().header(HEADER);
        configLoader.defaultOptions().shouldCopyDefaults(true);

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
                        try {
                            Throwables.propagateIfPossible(ex.getCause(), InvocationTargetException.class);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
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
        if(config.node(splitPath(path)).virtual()) {
            try {
                config.node(splitPath(path)).set(def);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean getBoolean(String path, boolean def) {
        set(path, def);
        return config.node(splitPath(path)).getBoolean(def);
    }

    private static double getDouble(String path, double def) {
        set(path, def);
        return config.node(splitPath(path)).getDouble(def);
    }

    private static int getInt(String path, int def) {
        set(path, def);
        return config.node(splitPath(path)).getInt(def);
    }

    private static String getString(String path, String def) {
        set(path, def);
        return config.node(splitPath(path)).getString(def);
    }

    private static Long getLong(String path, Long def) {
        set(path, def);
        return config.node(splitPath(path)).getLong(def);
    }

    public static String NO_PERMISSION = "<red>You do not have permission to do that.</red>";
    public static String NO_CONSOLE = "<red>You cannot use this command from console.</red>";

    private static void loadGeneric() {
        NO_PERMISSION = getString("generic.no-permission", NO_PERMISSION);
        NO_CONSOLE = getString("generic.no-console", NO_CONSOLE);
    }

    public static String NOT_WHITELISTED = "<red>You are not whitelisted on <server></red>";
    public static String ALREADY_OFF = "<gold>Whitelist was already <red>off</red>, nothing changed!</gold>";
    public static String ALREADY_ON = "<gold>Whitelist was already <green>on</green>, nothing changed!</gold>";
    public static String TURNED_OFF = "<gold>Turned the whitelist <red>off</red> for <server>!</gold>";
    public static String TURNED_ON = "<gold>Turned the whitelist <green>on</green> for <server>!</gold>";
    public static String INVALID_SERVER = "<red><server> is not a valid server.</red>";
    public static String ENFORCED_WHITELIST = "<green>Enforced whitelist for <server>.</green>";
    public static String WHITELIST_OFF = "<red>There is no whitelist on for <server>.</red>";
    private static void loadWhitelistMessages() {
        NOT_WHITELISTED = getString("messages.not-whitelisted", NOT_WHITELISTED);
        ALREADY_OFF = getString("messages.already-off", ALREADY_OFF);
        ALREADY_ON = getString("messages.already-on", ALREADY_ON);
        TURNED_OFF = getString("messages.turned-off", TURNED_OFF);
        TURNED_ON = getString("messages.turned-on", TURNED_ON);
        INVALID_SERVER = getString("messages.invalid-server", INVALID_SERVER);
        ENFORCED_WHITELIST = getString("messages.enforced-whitelist", ENFORCED_WHITELIST);
        WHITELIST_OFF = getString("messages.enforced-whitelist", WHITELIST_OFF);
    }

    public static String PW_HELP_MESSAGE_WRAPPER = "<gold>PermissionWhitelist help:\n<commands></gold>";
    public static String PW_HELP_COMMAND_OFF = "<gold>/permissionwhitelist off <server></gold>: <green>Turns the whitelist off for specified server</green>";
    public static String PW_HELP_COMMAND_ON = "<gold>/permissionwhitelist on <server></gold>: <green>Turns the whitelist on for specified server</green>";
    public static String PW_HELP_COMMAND_HELP = "<gold>/permissionwhitelist help</gold>: <green>Shows this menu</green>";
    public static String PW_HELP_COMMAND_ENFORCE = "<gold>/permissionwhitelist enforce <server></gold>: <green>Enforces whitelist for specified server</green>";
    private static void loadPermissionWhitelistHelp() {
        PW_HELP_MESSAGE_WRAPPER = getString("help.wrapper", PW_HELP_MESSAGE_WRAPPER);
        PW_HELP_COMMAND_OFF = getString("help.command-off", PW_HELP_COMMAND_OFF);
        PW_HELP_COMMAND_ON = getString("help.command-on", PW_HELP_COMMAND_ON);
        PW_HELP_COMMAND_HELP = getString("help.command-help", PW_HELP_COMMAND_HELP);
        PW_HELP_COMMAND_ENFORCE = getString("help.command-enforce", PW_HELP_COMMAND_ENFORCE);
    }

    public static String Q_HELP_MESSAGE_WRAPPER = "<gold>Queue help:\n<commands></gold>";
    public static String Q_HELP_COMMAND_HELP = "<gold>/queue help</gold>: <green>Shows this menu</green>";
    public static String Q_HELP_COMMAND_INFO = "<gold>/queue info [server]</gold>: <green>Displays info for the current queue</green>";
    public static String Q_HELP_COMMAND_LIST = "<gold>/queue list [server]</gold>: <green>Show the queue of a server</green>";
    public static String Q_HELP_COMMAND_LIST_ONLINE = "<gold>/queue listonline <server></gold>: <green>Show the online players of a server according to the plugin</green>";
    public static String Q_HELP_COMMAND_LEAVE = "<gold>/queue leave</gold>: <green>Leave the queue you're in</green>";
    public static String Q_HELP_COMMAND_RELOAD = "<gold>/queue reload</gold>: <green>Reload the queue plugin</green>";
    private static void loadCommandMessages() {
        String path = "queue-help.";

        Q_HELP_COMMAND_HELP = getString(path + "help", Q_HELP_COMMAND_HELP);
        Q_HELP_MESSAGE_WRAPPER = getString(path + "wrapper", Q_HELP_MESSAGE_WRAPPER);
        Q_HELP_COMMAND_INFO = getString(path + "info", Q_HELP_COMMAND_INFO);
        Q_HELP_COMMAND_LIST = getString(path + "list", Q_HELP_COMMAND_LIST);
        Q_HELP_COMMAND_LIST_ONLINE = getString(path + "list-online", Q_HELP_COMMAND_LIST_ONLINE);
        Q_HELP_COMMAND_LEAVE = getString(path + "leave", Q_HELP_COMMAND_LEAVE);
        Q_HELP_COMMAND_RELOAD = getString(path + "reload", Q_HELP_COMMAND_RELOAD);
    }

    public static String SENDING_TO_SERVER = "<green>Sending you to <server> after <time> minutes in queue!";
    public static String LIST_ONLINE = "<green><amount> online players on <server>:</green>\n<online_players>";
    public static String LIST_ONLINE_INVALID = "\n<red>Found <amount> invalid online players removing them from online player list:\n</red><invalid_players>";

    private static void loadOtherMessages() {
        String path = "other.";

        SENDING_TO_SERVER = getString(path + "sending-to-server", SENDING_TO_SERVER);
        LIST_ONLINE = getString(path + "list-online", LIST_ONLINE);
        LIST_ONLINE_INVALID = getString(path + "list-online-invalid", LIST_ONLINE_INVALID);
    }
}
