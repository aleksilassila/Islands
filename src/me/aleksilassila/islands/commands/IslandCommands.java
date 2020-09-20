package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.generation.IslandGrid;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class IslandCommands implements CommandExecutor {
    private Main plugin;

    public IslandCommands(Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("go").setExecutor(this);
        plugin.getCommand("island").setExecutor(this);
        plugin.getCommand("visit").setExecutor(this);
        plugin.getCommand("vi").setExecutor(this);

    }

    private Biome getTargetBiome(String biome) {
         Biome targetBiome = null;

         for (Biome b : Biome.values()) {
             if (b.name().equalsIgnoreCase(biome)) {
                 targetBiome = b;
             }
         }

         return targetBiome;
    }

    private void go(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage("Provide world.");
            return;
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
    }

    private void visit(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /visit name");
            return;
        }

        try {
            String islandId = plugin.islands.grid.getPrivateIsland(player.getUniqueId(), args[0]);

            player.teleport(plugin.islands.grid.getIslandSpawn(islandId));
        } catch (IslandGrid.IslandGridException e) {
            player.sendMessage(ChatColor.RED + "404 - Island not found.");
            return;
        }
    }

    private void createIsland(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /island create BIOME");

            HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

            for (Biome biome : availableLocations.keySet()) {
                if (availableLocations.get(biome).size() > 0) {
                    player.sendMessage(ChatColor.GREEN + biome.toString() + " has " + availableLocations.get(biome).size() + " island variations available.");
                }
            }

            return;
        }

        Biome targetBiome = getTargetBiome(args[1]);

        if (targetBiome == null) {
            player.sendMessage(ChatColor.RED + "Biome not found.");
            return;
        }

        try {
            String islandId = plugin.islands.createNewIsland(targetBiome, Islands.IslandSize.NORMAL, player.getUniqueId());

            player.sendMessage(ChatColor.GREEN + "Island created successfully.");
            try {
                player.teleport(plugin.islands.grid.getIslandSpawn(islandId));

            } catch (IslandGrid.IslandGridException e) {
                player.sendMessage(ChatColor.RED + "Could not teleport to island.");
            }
        } catch (Islands.IslandsException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        }

    }

    private void regenerateIsland(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /island regenerate name BIOME");
            return;
        }

        Biome targetBiome = getTargetBiome(args[2]);

        if (targetBiome == null) {
            player.sendMessage(ChatColor.RED + "Biome not found.");
            return;
        }

        boolean success = plugin.islands.regenerateIsland(targetBiome, player.getUniqueId(), args[1]);

        if (success) {
            player.sendMessage(ChatColor.GREEN + "Island regenerated successfully.");
            try {
                player.teleport(plugin.islands.grid.getIslandSpawn(plugin.islands.grid.getPrivateIsland(player.getUniqueId(), args[1])));
            } catch (IslandGrid.IslandGridException e) {
                player.sendMessage(ChatColor.RED + "Could not teleport to island.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Island regeneration failed.");
        }
    }

    private void listIslands(Player player, String[] args) {
         try {
            List<String> ids = plugin.islands.grid.getAllIslandIds(player.getUniqueId());

            player.sendMessage(ChatColor.GREEN + "Found " + ids.size() + " island(s).");
            for (String islandId : ids) {
                String name = plugin.getIslandsConfig().getString("islands." + islandId + ".name");
                player.sendMessage(ChatColor.AQUA + " - " + name);
            }
        } catch (IslandGrid.IslandGridException e) {
            return;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (label.equalsIgnoreCase("go")) {
                go(player, args);
                return true;

            } else if (label.equalsIgnoreCase("vi") || label.equalsIgnoreCase("visit")) {
                visit(player, args);
                return true;

            } else if (label.equalsIgnoreCase("island")) {
                if (args[0].equalsIgnoreCase("create")) {
                    createIsland(player, args);
                    return true;

                } else if (args[0].equalsIgnoreCase("regenerate")) {
                    regenerateIsland(player, args);
                    return true;


                } else if (args[0].equalsIgnoreCase("list")) {
                    listIslands(player, args);
                    return true;


                }
            }
        } else {
            // Server commands here
        }

        return true;
    }
}
