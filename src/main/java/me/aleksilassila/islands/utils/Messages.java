package me.aleksilassila.islands.utils;

import me.aleksilassila.islands.Plugin;
import org.bukkit.OfflinePlayer;

import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;

public class Messages {
    private static ResourceBundle bundle;
    private static ResourceBundle fallbackBundle;
    private static Map<String, String> messageCache;

    static final String BUNDLE_NAME = "messages";

    public static void init() {
        Messages.messageCache = new HashMap<>();

        Locale locale = new Locale(Optional.ofNullable(Plugin.instance.getConfig().getString("locale")).orElse("en"));

        try {
            URL[] urls = new URL[]{Plugin.instance.getDataFolder().toURI().toURL()};
            ClassLoader loader = new URLClassLoader(urls);
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, loader);
        } catch (Exception ignored) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        }

        fallbackBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);

        Plugin.instance.getLogger().info("Using " + locale.getDisplayName() + " locales");
    }

    public static void send(OfflinePlayer player, final String formatKey, final Object... objects) {
        if (player.getPlayer() != null)
            player.getPlayer().sendMessage(Messages.get(formatKey, objects));
    }

    public static String get(final String string, final Object... objects) {
        // Simple cache for messages
        if (objects.length == 0) {
            if (messageCache.containsKey(string))
                return messageCache.get(string);
            else {
                String message = getFormatString(string);
                messageCache.put(string, message);

                return message;
            }
        }

        return format(string, objects);
    }

    private static String getFormatString(final String string) {
        try {
            return bundle.getString(string);
        } catch (MissingResourceException e) {
            Plugin.instance.getLogger().severe("No translation found for " + string + ", used default translation");
            return fallbackBundle.getString(string);
        }
    }

    public static String format(final String string, final Object... objects) {
        String format = getFormatString(string);
        MessageFormat messageFormat;

        try {
            messageFormat = new MessageFormat(format);
        } catch (final IllegalArgumentException e) {
            Plugin.instance.getLogger().severe("Invalid Translation key for '" + string + "': " + e.getMessage());
            format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
            messageFormat = new MessageFormat(format);
        }

        return messageFormat.format(objects);
    }
}
