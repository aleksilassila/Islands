package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.generation.IslandGrid;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class IslandCommands {
    public static class VisitCommand implements CommandExecutor {
        private Main plugin;

        public VisitCommand(Main plugin) {
            this.plugin = plugin;

            plugin.getCommand("visit").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /visit name");
                return true;
            }

            try {
                String islandId = plugin.islands.grid.getPrivateIsland(player.getUniqueId(), args[0]);

                player.teleport(plugin.islands.grid.getIslandSpawn(islandId));
            } catch (IslandGrid.IslandGridException e) {
                player.sendMessage(ChatColor.RED + "404 - Island not found.");
            }

            return true;
        }
    }

    public static class HomeCommand implements CommandExecutor {
        private Main plugin;
        private IslandGrid grid;

        public HomeCommand(Main plugin) {
            this.plugin = plugin;
            this.grid = plugin.islands.grid;

            plugin.getCommand("home").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            String homeId = args.length == 0 ? "1" : args[0];

            try {
                player.teleport(grid.getIslandSpawn(grid.getHomeIsland(player.getUniqueId(), homeId)));
            } catch (IslandGrid.IslandGridException e) {
                player.sendMessage(ChatColor.RED + "404 - Home not found.");
            }

            return true;
        }
    }
}