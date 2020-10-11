package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
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
    private final Main plugin;
    private final IslandLayout layout;
    private final IslandManagmentCommands.Utils utils = new IslandManagmentCommands.Utils();

    public createSubcommand(Main plugin) {
        this.plugin = plugin;

        this.layout = plugin.islands.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!Permissions.checkPermission(player, Permissions.command.create)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        int islandSize = args.length == 2 ? utils.parseIslandSize(args[1]) : utils.parseIslandSize(String.valueOf(Islands.IslandSize.NORMAL.getSize()));

        String permissionRequired;

        if (islandSize == Islands.IslandSize.BIG.getSize()) {
            permissionRequired = Permissions.command.createBig;
        } else if (islandSize == Islands.IslandSize.SMALL.getSize()) {
            permissionRequired = Permissions.command.createSmall;
        } else if (islandSize == Islands.IslandSize.NORMAL.getSize()) {
            permissionRequired = Permissions.command.createNormal;
        } else {
            permissionRequired = Permissions.command.createCustom;
        }

        if (!Permissions.checkPermission(player, permissionRequired)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (islandSize < Islands.IslandSize.SMALL.getSize() || islandSize + 4 >= layout.islandSpacing) {
            player.sendMessage(Messages.error.INVALID_ISLAND_SIZE);
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


        int previousIslands = layout.getIslandIds(player.getUniqueId()).size();

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
