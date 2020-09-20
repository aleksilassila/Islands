package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class GenerationCommand extends ChatUtils implements CommandExecutor {
    private Main plugin;

    public GenerationCommand(Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("go").setExecutor(this);
        plugin.getCommand("island").setExecutor(this);

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

    private void createIsland(Player player, String[] args) {
        HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

        if (args.length != 2) {
            player.sendMessage( error("Usage: /island create BIOME"));

            for (Biome biome : availableLocations.keySet()) {
                if (availableLocations.get(biome).size() > 0) {
                    player.sendMessage(ChatColor.GOLD + biome.toString() + ChatColor.GREEN +  " has " + ChatColor.GOLD +  availableLocations.get(biome).size() + ChatColor.GREEN +  " island variations available.");
                }
            }

            return;
        }

        Biome targetBiome = getTargetBiome(args[1]);

        if (targetBiome == null) {
            player.sendMessage(error("Biome not found."));
            return;
        }

        if (!availableLocations.containsKey(targetBiome)) {
            player.sendMessage(error("No available locations for that biome."));
            return;
        }

        try {
            String islandId = plugin.islands.createNewIsland(targetBiome, Islands.IslandSize.NORMAL, player.getUniqueId());

            player.sendMessage(success("Island created successfully."));
            try {
                player.teleport(plugin.islands.grid.getIslandSpawn(islandId));

            } catch (IslandGrid.IslandGridException e) {
                player.sendMessage(error("Could not teleport to island."));
            }
        } catch (Islands.IslandsException e) {
            player.sendMessage(error(e.getMessage()));
        }

    }

    private void regenerateIsland(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(error("Usage: /island regenerate name BIOME"));
            return;
        }

        Biome targetBiome = getTargetBiome(args[2]);

        if (targetBiome == null) {
            player.sendMessage(error("Biome not found."));
            return;
        }

        HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

        if (!plugin.islands.islandGeneration.biomes.availableLocations.containsKey(targetBiome)) {
            player.sendMessage(error("No available locations for that biome."));
            return;
        }

        boolean success = plugin.islands.regenerateIsland(targetBiome, player.getUniqueId(), args[1]);

        if (success) {
            player.sendMessage(success("Island regenerated successfully."));
            try {
                player.teleport(plugin.islands.grid.getIslandSpawn(plugin.islands.grid.getPrivateIsland(player.getUniqueId(), args[1])));
            } catch (IslandGrid.IslandGridException e) {
                player.sendMessage(error("Could not teleport to island."));
            }
        } else {
            player.sendMessage(error("Island regeneration failed."));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args[0].equalsIgnoreCase("create")) {
                createIsland(player, args);

                return true;
            } else if (args[0].equalsIgnoreCase("regenerate")) {
                regenerateIsland(player, args);

                return true;
            }
        } else {
            // Server commands here
        }

        return true;
    }
}
