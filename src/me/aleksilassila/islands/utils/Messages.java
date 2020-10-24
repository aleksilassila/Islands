package me.aleksilassila.islands.utils;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class Messages extends ChatUtils {
        public static Messages instance;
        private static Islands plugin;

        private static ResourceBundle bundle;

        static String BUNDLE_NAME = "messages";

        public static Messages init(Islands plugin) {
            if (instance != null) {
                return instance;
            }

            instance = new Messages();
            Messages.plugin = plugin;

            Locale locale = new Locale(Optional.ofNullable(plugin.getConfig().getString("locale")).orElse("en"));

            try {
                URL[] urls = new URL[]{plugin.getDataFolder().toURI().toURL()};
                ClassLoader loader = new URLClassLoader(urls);
                bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, loader);
            } catch (Exception ignored) {
                bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            }

            plugin.getLogger().info("Using " + locale.getDisplayName() + " locales");

            return instance;

        }

        public static void send(Player player, final String string, final Object... objects) {
            player.sendMessage(Messages.get(string, objects));
        }

        public static String get(final String string, final Object... objects) {
            if (instance == null) {
                return "";
            }

            return instance.format(string, objects);
        }

        public String format(final String string, final Object... objects) {
            String format = bundle.getString(string);
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