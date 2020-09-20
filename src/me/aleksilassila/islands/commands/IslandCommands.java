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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (label.equalsIgnoreCase("go")) {
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

                player.teleport(new Location(world, player.getLocation().getBlockX(), 120, player.getLocation().getBlockZ()));

                return true;
            } else if (label.equalsIgnoreCase("vi") || label.equalsIgnoreCase("visit")) {
                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /visit name");
                    return true;
                }

                try {
                    String islandId = plugin.islands.grid.getIslandId(player.getUniqueId(), args[0]);

                    player.teleport(new Location(
                        plugin.islandsWorld, plugin.getConfig().getInt("islands." + islandId + ".spawnPoint.x"), 200, plugin.getConfig().getInt("islands." + islandId + ".spawnPoint.z")
                    ));
                } catch (IslandGrid.IslandGridException e) {
                    player.sendMessage(ChatColor.RED + "404 - Island not found.");
                    return true;
                }
            } else if (label.equalsIgnoreCase("island")) {
                if (args[0].equalsIgnoreCase("create")) {
                    if (args.length != 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /island create BIOME");

                        HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

                        for (Biome biome : availableLocations.keySet()) {
                            if (availableLocations.get(biome).size() > 0) {
                                player.sendMessage(ChatColor.GREEN + biome.toString() + " has " + availableLocations.get(biome).size() + " island variations available.");
                            }
                        }

                        return true;
                    }

                    Biome targetBiome = getTargetBiome(args[1]);

                    if (targetBiome == null) {
                        player.sendMessage(ChatColor.RED + "Biome not found.");
                        return true;
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

                    return true;


                } else if (args[0].equalsIgnoreCase("regenerate")) {
                    if (args.length != 3) {
                        player.sendMessage(ChatColor.RED + "Usage: /island regenerate name BIOME");
                        return true;
                    }

                    Biome targetBiome = getTargetBiome(args[2]);

                    if (targetBiome == null) {
                        player.sendMessage(ChatColor.RED + "Biome not found.");
                        return true;
                    }

                    boolean success = plugin.islands.regenerateIsland(targetBiome, player.getUniqueId(), args[1]);

                    if (success) {
                        player.sendMessage(ChatColor.GREEN + "Island regenerated successfully.");
                        try {
                            player.teleport(plugin.islands.grid.getIslandSpawn(plugin.islands.grid.getIslandId(player.getUniqueId(), args[1])));
                        } catch (IslandGrid.IslandGridException e) {
                            player.sendMessage(ChatColor.RED + "Could not teleport to island.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Island regeneration failed.");
                    }

                    return true;


                } else if (args[0].equalsIgnoreCase("available")) {
                    HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

                    for (Biome biome : availableLocations.keySet()) {
                        if (availableLocations.get(biome).size() > 0) {
                            player.sendMessage(ChatColor.GREEN + biome.toString() + " has " + availableLocations.get(biome).size() + " island variations available.");
                        }
                    }

                    return true;


                } else if (args[0].equalsIgnoreCase("list")) {
                    try {
                        List<String> ids = plugin.islands.grid.getAllIslandIds(player.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + "Found " + ids.size() + " island(s).");
                        for (String islandId : ids) {
                            String name = plugin.getConfig().getString("islands." + islandId + ".name");
                            player.sendMessage(ChatColor.AQUA + " - " + name);
                        }
                    } catch (IslandGrid.IslandGridException e) {
                        return true;
                    }

                    return true;


                } else if (args[0].equalsIgnoreCase("search")) {
                    if (args.length != 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /island search BIOME");
                        return true;
                    }

                    Biome targetBiome = getTargetBiome(args[1]);

                    if (targetBiome == null) {
                        player.sendMessage(ChatColor.RED + "Biome not found.");
                        return true;
                    }

                    List<Location> locations = plugin.islands.islandGeneration.biomes.getPossibleIslandLocations(targetBiome, 32);

                    if (locations.size() == 0) {
                        player.sendMessage(ChatColor.RED + "No suitable locations found.");
                        return true;
                    }



                    for (Location loc : locations) {
                        player.sendMessage(ChatColor.GREEN + "Found location " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                    }

                    if (locations.size() == 0) {
                        player.sendMessage(ChatColor.RED + "No locations found. See all locations: /islands");
                    }

                    return true;
                }
            }
        }

        return true;
    }
}
