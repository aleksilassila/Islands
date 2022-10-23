package me.aleksilassila.islands.commands;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class Subcommand {
    public abstract void onCommand(Player player, String[] args, boolean confirmed);
    public abstract List<String> onTabComplete(Player player, String[] args);
    public abstract String getName();
    public abstract String help();
    public abstract String getPermission();
}
