package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.GUIs.CreateGUI;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class CreateSubcommand extends GenerationSubcommands {
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

        if (!validateCommand(player, islandSize)) return;

        HashMap<Biome, List<Location>> availableLocations = plugin.islandGeneration.biomes.availableLocations;

        if (args.length < 1) {
            new CreateGUI(plugin, player, "create").open();

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

        if (plugin.econ != null && !hasFunds(player, plugin.islandPrices.getOrDefault(islandSize, 0.0))) {
            player.sendMessage(Messages.get("error.INSUFFICIENT_FUNDS"));
            return;
        }

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

        if (plugin.econ != null) pay(player, plugin.islandPrices.getOrDefault(islandSize, 0.0));
        player.sendTitle(Messages.get("success.ISLAND_GEN_TITLE"), Messages.get("success.ISLAND_GEN_SUBTITLE"), 10, 20 * 7, 10);
    }

    @Override
    Islands getPlugin() {
        return plugin;
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
}
