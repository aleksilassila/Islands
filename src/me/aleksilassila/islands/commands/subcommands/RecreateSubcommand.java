package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.GUIs.CreateGUI;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class RecreateSubcommand extends GenerationSubcommands {
    private final Islands plugin;
    private final IslandLayout layout;

    private final IslandCommands.Utils utils = new IslandCommands.Utils();

    public RecreateSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        int islandSize = args.length == 2 ? plugin.parseIslandSize(args[1]) : plugin.parseIslandSize("");

        if (!validateCommand(player, islandSize)) return;

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.get("error.WRONG_WORLD"));
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.get("error.NOT_ON_ISLAND"));
            return;
        } else if (!layout.getUUID(islandId).equals(player.getUniqueId().toString())
                && !player.hasPermission(Permissions.bypass.recreate)) {
            player.sendMessage(Messages.get("error.UNAUTHORIZED"));
            return;
        }

        if (args.length < 1) {
            CreateGUI gui = new CreateGUI(plugin, player, "recreate");

            if (plugin.econ != null && plugin.getConfig().getBoolean("economy.recreateSum")) {
                double oldCost = plugin.islandPrices.getOrDefault(plugin.getIslandsConfig().getInt(islandId + ".size"), 0.0);
                gui.setOldCost(oldCost);
            }

            gui.open();
            return;
        }

        double cost = 0.0;

        if (plugin.econ != null) {
            cost = plugin.islandPrices.getOrDefault(islandSize, 0.0);
            cost += plugin.getConfig().getDouble("economy.recreateCost");

            if (plugin.getConfig().getBoolean("economy.recreateSum")) {
                double oldCost = plugin.islandPrices.getOrDefault(plugin.getIslandsConfig().getInt(islandId + ".size"), 0.0);

                cost = Math.max(cost - oldCost, 0);
            }

            if (!hasFunds(player, cost)) {
                player.sendMessage(Messages.get("error.INSUFFICIENT_FUNDS"));
                return;
            }
        }

        Biome targetBiome;

        if (args[0].equalsIgnoreCase("random") && !isRandomBiomeDisabled()) {
            targetBiome = null;
        } else {
            targetBiome = utils.getTargetBiome(args[0]);

            if (targetBiome == null) {
                player.sendMessage(Messages.get("error.NO_BIOME_FOUND"));
                return;
            }


            if (!plugin.islandGeneration.biomes.availableLocations.containsKey(targetBiome)) {
                player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));
                return;
            }
        }

        if (!confirmed) {
            player.sendMessage(Messages.get("info.CONFIRM"));
            return;
        }

        try {
            boolean success = plugin.recreateIsland(islandId, targetBiome, islandSize, player);

            if (!success) {
                player.sendMessage(Messages.get("error.ONGOING_QUEUE_EVENT"));
                return;
            }

            if (plugin.econ != null) pay(player, cost);

            player.sendTitle(Messages.get("success.ISLAND_GEN_TITLE"), Messages.get("success.ISLAND_GEN_SUBTITLE"), 10, 20 * 7, 10);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));
        }
    }

    @Override
    Islands getPlugin() {
        return plugin;
    }

    @Override
    public String getName() {
        return "recreate";
    }

    @Override
    public String help() {
        return "Recreate island";
    }

    @Override
    public String getPermission() {
        return Permissions.command.recreate;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
