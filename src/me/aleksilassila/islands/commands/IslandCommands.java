package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IslandCommands {
    public static class UntrustCommand extends ChatUtils implements CommandExecutor {
        private Main plugin;

        public UntrustCommand(Main plugin) {
            this.plugin = plugin;

            plugin.getCommand("untrust").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            if (args.length != 1) {
                player.sendMessage(info("/untrust <player> (You have to be on target island)"));
                return true;
            }

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.islands.grid.getIslandId(player.getLocation());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(error("You have to be on an island."));
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString())) {
                player.sendMessage(error("You don't own this island."));
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[1]);

            if (targetPlayer == null) {
                player.sendMessage(error("Player not found."));
                return true;
            }

            plugin.islands.grid.removeTrusted(islandId, targetPlayer.getUniqueId().toString());

            player.sendMessage(success("Player untrusted!"));

            return true;
        }
    }

    public static class TrustCommand extends ChatUtils implements CommandExecutor {
        private Main plugin;

        public TrustCommand(Main plugin) {
            this.plugin = plugin;

            plugin.getCommand("trust").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) return false;

            Player player = (Player) sender;

            if (args.length != 1) {
                player.sendMessage(info("/trust <player> (You have to be on target island)"));
                return true;
            }

            String ownerUUID = plugin.islands.grid.getBlockOwnerUUID(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            String islandId = plugin.islands.grid.getIslandId(player.getLocation());

            if (ownerUUID == null || islandId == null) {
                player.sendMessage(error("You have to be on an island."));
                return true;
            }

            if (!ownerUUID.equals(player.getUniqueId().toString())) {
                player.sendMessage(error("You don't own this island."));
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[1]);

            if (targetPlayer == null) {
                player.sendMessage(error("Player not found."));
                return true;
            }

            plugin.islands.grid.addTrusted(islandId, targetPlayer.getUniqueId().toString());

            player.sendMessage(success("Player trusted!"));

            return true;
        }
    }

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


            String islandId = plugin.islands.grid.getPublicIsland(args[0]);

            if (islandId != null) {
                player.teleport(plugin.islands.grid.getIslandSpawn(islandId));
            } else {
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
                List<String> ids = plugin.islands.grid.getAllIslandIds(player.getUniqueId());

                player.sendMessage(success("Found " + ids.size() + " home(s)."));
                for (String islandId : ids) {
                    String name = plugin.getIslandsConfig().getString("islands." + islandId + ".name");
                    String homeNumber = plugin.getIslandsConfig().getString("islands." + islandId + ".home");
                    player.sendMessage(ChatColor.AQUA + " - " + name + " (" + homeNumber + ")");
                }

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

            Location location = grid.getIslandSpawn(grid.getHomeIsland(player.getUniqueId(), homeId));

            if (location != null) {
                player.teleport(location);
            } else {
                player.sendMessage(error("404 - Home not found."));
            }

            return true;
        }
    }
}