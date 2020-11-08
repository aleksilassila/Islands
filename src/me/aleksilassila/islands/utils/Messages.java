package me.aleksilassila.islands.utils;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;

public class Messages extends ChatUtils {
        public static Messages instance;
        private static Islands plugin;

        private static ResourceBundle bundle;
        private static ResourceBundle fallbackBundle;
        private static Map<String, String> messageCache;

        static String BUNDLE_NAME = "messages";

        public static Messages init(Islands plugin) {
            if (instance != null) {
                return instance;
            }

            instance = new Messages();
            Messages.plugin = plugin;
            Messages.messageCache = new HashMap<>();

            Locale locale = new Locale(Optional.ofNullable(plugin.getConfig().getString("locale")).orElse("en"));

            try {
                URL[] urls = new URL[]{plugin.getDataFolder().toURI().toURL()};
                ClassLoader loader = new URLClassLoader(urls);
                bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, loader);
            } catch (Exception ignored) {
                bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            }

            fallbackBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);

            plugin.getLogger().info("Using " + locale.getDisplayName() + " locales");

            return instance;
        }

        public static void send(OfflinePlayer player, final String string, final Object... objects) {
            if (player.getPlayer() != null)
                player.getPlayer().sendMessage(Messages.get(string, objects));
        }

        public static String get(final String string, final Object... objects) {
            if (instance == null) {
                return "";
            }

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
                plugin.getLogger().severe("No translation found for " + string + ", used default translation");
                return fallbackBundle.getString(string);
            }
        }

        public static String format(final String string, final Object... objects) {
            String format = getFormatString(string);
            MessageFormat messageFormat;

            try {
                messageFormat = new MessageFormat(format);
            } catch (final IllegalArgumentException e) {
                plugin.getLogger().severe("Invalid Translation key for '" + string + "': " + e.getMessage());
                format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
                messageFormat = new MessageFormat(format);
            }

            return messageFormat.format(objects);
        }

        public static class info {
            public static String VERSION_INFO(String version) {
                return info("Islands " + version);
            }
        }

        public static class help {
            public static final String UNTRUST = info("/untrust <player> (You have to be on target island)");
            public static final String TRUST = info("/trust <player> (You have to be on target island)");
            public static final String CREATE = ChatColor.GRAY + "/island create <biome> (<SIZE>)";
            public static final String RECREATE = ChatColor.GRAY + "/island recreate <biome> (<SIZE>) (You have to be on target island)";
            public static final String NAME = ChatColor.GRAY + "/island name <name> (You have to be on target island)";
            public static final String UNNAME = ChatColor.GRAY + "/island unname (You have to be on target island)";
            public static final String GIVE = ChatColor.GRAY + "/island give <name> (You have to be on target island)";
            public static final String DELETE = ChatColor.GRAY + "/island delete (You have to be on target island)";
            public static final String HOME = error("Usage: /home <id>");
            public static final String AVAILABLE_COMMANDS = ChatColor.WHITE + "Here's a list of subcommands you have access to:";

            public static String SUBCOMMAND(Subcommand subcommand) {
                return ChatColor.WHITE + "" + ChatColor.BOLD + subcommand.getName() + ChatColor.RESET + ChatColor.GRAY + ": " + subcommand.help();
            }
        }
    }