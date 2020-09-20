package me.aleksilassila.islands.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatUtils {
    public String pluginTag = ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "Islands" + ChatColor.GOLD + "]" + ChatColor.WHITE + " ";

    public String error(String message) {
        return pluginTag + ChatColor.RED + message;
    }

    public String success(String message) {
        return pluginTag + ChatColor.GREEN + message;
    }

    public String info(String message) {
        return pluginTag + ChatColor.GRAY + message;
    }
}
