package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class regenerateSubcommand extends Subcommand {
    private Main plugin;
    private IslandGrid grid;

    private IslandManagmentCommands.Utils utils = new IslandManagmentCommands.Utils();

    public regenerateSubcommand(Main plugin) {
        this.plugin = plugin;
        this.grid = plugin.islands.grid;
    }

    private boolean isSmallerThanOldIsland(Islands.IslandSize newSize, String islandId) {
        return plugin.islands.parseIslandSize(newSize) < plugin.getIslandsConfig().getInt("islands." + islandId + ".size");
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;
        Islands.IslandSize islandSize;
        String permissionRequired;
        String islandId;
        Biome targetBiome;

        if (!Permissions.checkPermission(player, Permissions.command.regenerate)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        islandSize = args.length == 2 ? utils.parseIslandSize(args[1]) : Islands.IslandSize.NORMAL;

        switch (islandSize) {
            case BIG:
                permissionRequired = Permissions.command.createBig;
                break;
            case SMALL:
                permissionRequired = Permissions.command.createSmall;
                break;
            case NORMAL:
            default:
                permissionRequired = Permissions.command.createNormal;
                break;
        }

        if (!Permissions.checkPermission(player, permissionRequired)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        if (args.length < 1) {
            player.sendMessage(Messages.help.REGENERATE);

            for (Biome biome : availableLocations.keySet()) {
                if (availableLocations.get(biome).size() > 0) {
                    player.sendMessage(ChatColor.GOLD + biome.toString() + ChatColor.GREEN +  " has " + ChatColor.GOLD
                            +  availableLocations.get(biome).size() + ChatColor.GREEN +  " island variations available.");
                }
            }

            return;
        }

        islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null ||
                (!plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                && !Permissions.checkPermission(player, Permissions.bypass.regenerate))) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        targetBiome = utils.getTargetBiome(args[0]);

        if (targetBiome == null) {
            player.sendMessage(Messages.error.NO_BIOME_FOUND);
            return;
        }

        if (!plugin.islands.islandGeneration.biomes.availableLocations.containsKey(targetBiome)) {
            player.sendMessage(Messages.error.NO_LOCATIONS_FOR_BIOME);
            return;
        }

        if (!confirmed) {
            player.sendMessage(Messages.info.CONFIRM);
            return;
        }

        try {
            boolean success = plugin.islands.regenerateIsland(islandId, targetBiome, islandSize, player, isSmallerThanOldIsland(islandSize, islandId));

            if (!success) {
                player.sendMessage(Messages.error.ONGOING_QUEUE_EVENT);
                return;
            }

            player.sendTitle(Messages.success.ISLAND_GEN_TITLE, Messages.success.ISLAND_GEN_SUBTITLE, 10, 20 * 7, 10);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Messages.error.NO_LOCATIONS_FOR_BIOME);
        }
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;
            List<String> availableArgs = new ArrayList<>();

            for (Biome biome : availableLocations.keySet()) {
                availableArgs.add(biome.name());
            }

            return availableArgs;
        } else if (args.length == 2) {
            return new ArrayList<>(Arrays.asList("BIG", "NORMAL", "SMALL"));
        }

        return null;
    }

    @Override
    public String getName() {
        return "regenerate";
    }

    @Override
    public String help() {
        return Messages.help.REGENERATE;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
