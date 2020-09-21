package me.aleksilassila.islands.utils;

import org.bukkit.ChatColor;

public class ChatUtils {
    public static String pluginTag = ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "Islands" + ChatColor.GOLD + "]" + ChatColor.WHITE + " ";

    public static String error(String message) {
        return pluginTag + ChatColor.RED + message;
    }

    public static String success(String message) {
        return pluginTag + ChatColor.GREEN + message;
    }

    public static String info(String message) {
        return pluginTag + ChatColor.GRAY + message;
    }
}
