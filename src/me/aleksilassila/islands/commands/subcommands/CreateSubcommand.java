package me.aleksilassila.islands.commands.subcommands;

import com.mojang.brigadier.Message;
import me.aleksilassila.islands.GUIs.CreateGUI;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateSubcommand extends CreationSubcommand {
    private final Islands plugin;
    private final IslandLayout layout;
    private final IslandManagmentCommands.Utils utils = new IslandManagmentCommands.Utils();

    public CreateSubcommand(Islands plugin) {
        this.plugin = plugin;

        this.layout = plugin.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        int islandSize = args.length == 2 ? plugin.parseIslandSize(args[1]) : plugin.parseIslandSize("");

        String permissionRequired = plugin.getCreatePermission(islandSize);

        if (!player.hasPermission(permissionRequired)) {
            player.sendMessage(Messages.get("error.NO_PERMISSION"));
            return;
        }

        if (islandSize < plugin.getSmallestIslandSize() || islandSize + 4 >= layout.islandSpacing) {
            player.sendMessage(Messages.get("error.INVALID_ISLAND_SIZE"));
            return;
        }

        HashMap<Biome, List<Location>> availableLocations = plugin.islandGeneration.biomes.availableLocations;

        if (args.length < 1) {
            new CreateGUI(plugin, player).open();

            //            player.sendMessage(Messages.help.CREATE);
//
//            for (Biome biome : availableLocations.keySet()) {
//                if (availableLocations.get(biome).size() > 0) {
//                    player.sendMessage(ChatColor.GOLD + biome.toString() + ChatColor.GREEN +  " has " + ChatColor.GOLD
//                            +  availableLocations.get(biome).size() + ChatColor.GREEN +  " island variations available.");
//                }
//            }

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

        if (previousIslands >= islandsLimit && !player.hasPermission(Permissions.bypass.create)) {
            player.sendMessage(Messages.get("error.ISLAND_LIMIT"));
            return;
        }

        Biome targetBiome = utils.getTargetBiome(args[0]);

        if (targetBiome == null) {
            player.sendMessage(Messages.get("error.NO_BIOME_FOUND"));
            return;
        }

        if (!buy(player, islandSize)) return;

        if (!availableLocations.containsKey(targetBiome)) {
            player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));
            return;
        }

        String islandId = null;

        try {
            islandId = plugin.createNewIsland(targetBiome, islandSize, player);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));

            return;
        }

        if (islandId == null) {
            player.sendMessage(Messages.get("error.ONGOING_QUEUE_EVENT"));
            return;
        }

        player.sendTitle(Messages.get("success.ISLAND_GEN_TITLE"), Messages.get("success.ISLAND_GEN_SUBTITLE"), 10, 20 * 7, 10);
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        List<String> availableArgs = new ArrayList<>();

        if (args.length == 1) {
            HashMap<Biome, List<Location>> availableLocations = plugin.islandGeneration.biomes.availableLocations;

            for (Biome biome : availableLocations.keySet()) {
                availableArgs.add(biome.name());
            }
        } else if (args.length == 2) {
            availableArgs.addAll(plugin.definedIslandSizes.keySet());
        }

        return availableArgs;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String help() {
        return "Create new island";
    }

    @Override
    public String getPermission() {
        return Permissions.command.create;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    protected Islands getPlugin() {
        return plugin;
    }
}
