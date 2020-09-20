package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GoCommand implements CommandExecutor {
    private Main plugin;

    public GoCommand (Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("go").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This is for players only.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("Provide world.");
            return true;
        }

        World world;

        if (args[0].equalsIgnoreCase("source")) {
            world = plugin.islandsSourceWorld;
        } else if (args[0].equalsIgnoreCase("islands")) {
            world = plugin.islandsWorld;
        } else {
            world = Bukkit.getWorlds().get(0);
        }

        player.teleport(new Location(world, player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));

        return true;
    }
}
