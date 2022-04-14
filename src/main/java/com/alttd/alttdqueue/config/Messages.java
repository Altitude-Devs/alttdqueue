package com.alttd.alttdqueue.config;

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

public final class Messages {

    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "Messages";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YAMLConfigurationLoader configLoader;

    static int version;
    static boolean verbose;

    public static void init(File path) {
        CONFIG_FILE = new File(path, "messages.yml");;
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

    public static String HELP_MESSAGE_WRAPPER = "<gold>PermissionWhitelist help:\n<commands></gold>";
    public static String HELP_COMMAND_OFF = "<gold>/permissionwhitelist off <server></gold>: <green>Turns the whitelist off for specified server</green>";
    public static String HELP_COMMAND_ON = "<gold>/permissionwhitelist on <server></gold>: <green>Turns the whitelist on for specified server</green>";
    public static String HELP_COMMAND_HELP = "<gold>/permissionwhitelist help</gold>: <green>Shows this menu</green>";
    public static String HELP_COMMAND_ENFORCE = "<gold>/permissionwhitelist enforce <server></gold>: <green>Enforces whitelist for specified server</green>";
    private static void loadCommandMessages() {
        HELP_MESSAGE_WRAPPER = getString("help.wrapper", HELP_MESSAGE_WRAPPER);
        HELP_COMMAND_OFF = getString("help.command-off", HELP_COMMAND_OFF);
        HELP_COMMAND_ON = getString("help.command-on", HELP_COMMAND_ON);
        HELP_COMMAND_HELP = getString("help.command-help", HELP_COMMAND_HELP);
        HELP_COMMAND_ENFORCE = getString("help.command-enforce", HELP_COMMAND_ENFORCE);
    }
}
