package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IslandCommands {
    public static class VisitCommand extends ChatUtils implements CommandExecutor {
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
                player.sendMessage(info("Usage: /visit name"));
                return true;
            }

            try {
                String islandId = plugin.islands.grid.getPrivateIsland(player.getUniqueId(), args[0]);

                player.teleport(plugin.islands.grid.getIslandSpawn(islandId));
            } catch (IslandGrid.IslandNotFound e) {
                player.sendMessage(error("404 - Island not found."));
            }

            return true;
        }
    }

    public static class HomeCommand extends ChatUtils implements CommandExecutor {
        private Main plugin;
        private IslandGrid grid;

        public HomeCommand(Main plugin) {
            this.plugin = plugin;
            this.grid = plugin.islands.grid;

            plugin.getCommand("home").setExecutor(this);
            plugin.getCommand("homes").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This is for players only.");
                return false;
            }

            Player player = (Player) sender;

            if (args.length == 1 && args[0].equalsIgnoreCase("list") || label.equalsIgnoreCase("homes")) {
                try {
                    List<String> ids = plugin.islands.grid.getAllIslandIds(player.getUniqueId());

                    player.sendMessage(success("Found " + ids.size() + " home(s)."));
                    for (String islandId : ids) {
                        String name = plugin.getIslandsConfig().getString("islands." + islandId + ".name");
                        player.sendMessage(ChatColor.AQUA + " - " + name);
                    }
                } catch (IslandGrid.IslandNotFound ignored) { }

                return true;
            } else {
                try {
                    if (args.length != 0) {
                        Integer.parseInt(args[0]);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(error("Usage: /home <id>"));
                    return true;
                }
            }

            if (player.getWorld().getName().equals("world_nether")) {
                player.sendMessage(info("You can only use this command in overworld."));
                return true;
            }

            if (player.getWorld().getName().equals("world")) {
                // Check if is on surface
                Location playerLocation = player.getLocation();

                for (int y = playerLocation.getBlockY(); y < player.getWorld().getHighestBlockYAt(playerLocation); y++) {
                    playerLocation.setY(y);
                    if (player.getWorld().getBlockAt(playerLocation).getBlockData().getMaterial().equals(Material.STONE)) {
                        player.sendMessage(info("You can only use this command on surface."));
                        return true;
                    }
                }
            }

            String homeId = args.length == 0 ? "1" : args[0];

            try {
                player.teleport(grid.getIslandSpawn(grid.getHomeIsland(player.getUniqueId(), homeId)));
            } catch (IslandGrid.IslandNotFound e) {
                player.sendMessage(error("404 - Home not found."));
            }

            return true;
        }
    }
}