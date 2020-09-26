package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.Permissions;
import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class createSubcommand extends Subcommand {
    private Main plugin;
    private IslandGrid grid;
    private IslandManagmentCommands.Utils utils = new IslandManagmentCommands.Utils();

    public createSubcommand(Main plugin) {
        this.plugin = plugin;

        this.grid = plugin.islands.grid;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!Permissions.checkPermission(player, Permissions.island.create)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        Islands.IslandSize islandSize = args.length == 2 ? utils.parseIslandSize(args[1]) : Islands.IslandSize.NORMAL;

        String permissionRequired;

        switch (islandSize) {
            case BIG:
                permissionRequired = Permissions.island.createBig;
                break;
            case SMALL:
                permissionRequired = Permissions.island.createSmall;
                break;
            case NORMAL:
            default:
                permissionRequired = Permissions.island.createNormal;
                break;
        }

        if (!Permissions.checkPermission(player, permissionRequired)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

        if (args.length < 1) {
            player.sendMessage(Messages.help.CREATE);

            for (Biome biome : availableLocations.keySet()) {
                if (availableLocations.get(biome).size() > 0) {
                    player.sendMessage(ChatColor.GOLD + biome.toString() + ChatColor.GREEN +  " has " + ChatColor.GOLD
                            +  availableLocations.get(biome).size() + ChatColor.GREEN +  " island variations available.");
                }
            }

            return;
        }


        int previousIslands = grid.getAllIslandIds(player.getUniqueId()).size();

        int islandsLimit = plugin.getConfig().getInt("defaultIslandLimit");

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("groupLimits");

        if (plugin.perms != null && section != null) {
            for (String group : plugin.perms.getGroups()) {
                if (plugin.perms.playerInGroup(player, group) && section.getInt(group) > islandsLimit) {
                    islandsLimit = section.getInt(group);
                }
            }
        }

        if (previousIslands >= islandsLimit && !Permissions.checkPermission(player, Permissions.bypass.create)) {
            player.sendMessage(Messages.error.ISLAND_LIMIT);
            return;
        }

        Biome targetBiome = utils.getTargetBiome(args[0]);

        if (targetBiome == null) {
            player.sendMessage(Messages.error.NO_BIOME_FOUND);
            return;
        }

        if (!availableLocations.containsKey(targetBiome)) {
            player.sendMessage(Messages.error.NO_LOCATIONS_FOR_BIOME);
            return;
        }

        String islandId = null;

        try {
            islandId = plugin.islands.createNewIsland(targetBiome, islandSize, player);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Messages.error.NO_LOCATIONS_FOR_BIOME);

            return;
        }

        if (islandId == null) {
            player.sendMessage(Messages.error.ONGOING_QUEUE_EVENT);
            return;
        }

        player.sendTitle(Messages.success.ISLAND_GEN_TITLE, Messages.success.ISLAND_GEN_SUBTITLE, 10, 20 * 7, 10);
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
        return "create";
    }

    @Override
    public String help() {
        return Messages.help.CREATE;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
