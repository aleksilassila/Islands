package me.aleksilassila.islands.utils;

import org.bukkit.ChatColor;

public class ChatUtils {
    public static String pluginTag = ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "Islands" + ChatColor.GOLD + "]" + ChatColor.WHITE + " ";

    public static String generateTag = ChatColor.DARK_AQUA + "[" + ChatColor.DARK_GREEN + "Generation" + ChatColor.DARK_AQUA + "]" + ChatColor.GRAY + " ";

    public static String generate(String message) { return generateTag + message; }

    public static String error(String message) { return ChatColor.RED + message; }

    public static String success(String message) { return ChatColor.GREEN + message; }

    public static String info(String message) { return ChatColor.GRAY + message; }
}
