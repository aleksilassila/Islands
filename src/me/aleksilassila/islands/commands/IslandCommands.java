package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class IslandCommands implements CommandExecutor {
    private Islands plugin;

    public IslandCommands(Islands plugin) {
        this.plugin = plugin;

        plugin.getCommand("go").setExecutor(this);
        plugin.getCommand("findisland").setExecutor(this);
        plugin.getCommand("createisland").setExecutor(this);

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

                player.sendMessage("Length of worlds: " + Bukkit.getWorlds().size());

                player.teleport(new Location(world, player.getLocation().getBlockX(), 120, player.getLocation().getBlockZ()));
            } else if (label.equalsIgnoreCase("findisland")) {
                if (args.length < 1) {
                    player.sendMessage("Provide biome.");
                    return true;
                }

                Biome targetBiome = null;

                for (Biome biome : Biome.values()) {
                    if (biome.name().equalsIgnoreCase(args[0])) {
                        targetBiome = biome;
                    }
                }

                if (targetBiome == null) {
                    player.sendMessage("Biome not found.");
                    return true;
                }

                List<Location> locations = plugin.islandGen.getAllIslandLocations(32, targetBiome);
                player.sendMessage("Found " + locations.size() + " suitable " + targetBiome + ".");
                for (Location location : locations) {
                    player.sendMessage("Location: " + location.getBlockX() + ", " + location.getBlockZ());
                }
            } else if (label.equalsIgnoreCase("createisland")) {
                if (args.length < 1) {
                    player.sendMessage("Provide biome.");
                    return true;
                }

                Biome targetBiome = null;

                for (Biome biome : Biome.values()) {
                    if (biome.name().equalsIgnoreCase(args[0])) {
                        targetBiome = biome;
                    }
                }

                if (targetBiome == null) {
                    player.sendMessage("Biome not found.");
                    return true;
                }

                boolean success = plugin.islandGen.generateIsland(targetBiome, 32);

//                List<Location> locations = plugin.islandGen.getAllIslandLocations(32, targetBiome);
//                Location location = locations.get(0);
//                Location location = new Location(plugin.islandsSourceWorld, 0,0,0);
//                plugin.islandGen.copyRegion(
//                        location,
//                        new Location(plugin.islandsSourceWorld, location.getBlockX() + 32, location.getBlockY() - 100, location.getBlockZ() + 32),
//                        location
//                );
                if (success) {
                    player.sendMessage("Done");
                } else {
                    player.sendMessage("Error occured");
                }

            }

            return true;
        }

        return false;
    }
}
