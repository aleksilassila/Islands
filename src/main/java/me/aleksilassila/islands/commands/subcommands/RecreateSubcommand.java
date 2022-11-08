package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Entry;
import me.aleksilassila.islands.GUIs.CreateGUI;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractCreateSubcommands;
import me.aleksilassila.islands.plugins.Economy;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.Utils;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class RecreateSubcommand extends AbstractCreateSubcommands {
    private final Economy economy;

    public RecreateSubcommand(Islands islands) {
        super(islands);

        this.economy = islands.economy;
    }

    @Override
    protected void openGui(Player player) {
        Entry island = getIsland(player);
        if (island == null) return;

        CreateGUI gui = new CreateGUI(islands, player, "recreate");

        if (plugin.econ != null && plugin.getConfig().getBoolean("economy.recreateSum")) {
            double oldCost = plugin.islandPrices.getOrDefault(island.size, 0.0);
            gui.setOldCost(oldCost);
        }

        gui.open();
    }

    private Entry getIsland(Player player) {
        if (!player.getWorld().equals(islandsWorld)) {
            player.sendMessage(Messages.get("error.WRONG_WORLD"));
            return null;
        }

        Entry island = islands.islandsConfig.getEntry(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), true);

        if (island == null) {
            player.sendMessage(Messages.get("error.NOT_ON_ISLAND"));
            return null;
        } else if (!player.getUniqueId().equals(island.uuid)
                && !player.hasPermission(Permissions.bypass.recreate)) {
            player.sendMessage(Messages.get("error.UNAUTHORIZED"));
            return null;
        }

        return island;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, int islandSize) {
        if (args.length > 2) {
            Messages.send(player, "usage.RECREATE");
            return;
        }

        Entry island = getIsland(player);
        if (island == null) return;

        double cost = 0.0;

        if (economy.isEnabled()) {
            cost = plugin.islandPrices.getOrDefault(islandSize, 0.0);
            cost += plugin.getConfig().getDouble("economy.recreateCost");

            if (plugin.getConfig().getBoolean("economy.recreateSum")) {
                double oldCost = plugin.islandPrices.getOrDefault(island.size, 0.0);

                cost = Math.max(cost - oldCost, 0);
            }

            if (!economy.hasFunds(player, cost)) {
                player.sendMessage(Messages.get("error.INSUFFICIENT_FUNDS"));
                return;
            }
        }

        Biome targetBiome;

        if (args[0].equalsIgnoreCase("random") && !isRandomBiomeDisabled()) {
            targetBiome = null;
        } else {
            targetBiome = Utils.getTargetBiome(args[0]);

            if (targetBiome == null) {
                player.sendMessage(Messages.get("error.NO_BIOME_FOUND"));
                return;
            }


            if (!islands.sourceWorld.getAvailableLocations().containsKey(targetBiome)) {
                player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));
                return;
            }
        }

        if (!confirmed) {
            player.sendMessage(Messages.get("info.CONFIRM"));
            return;
        }

        try {
            boolean success = islands.islandsWorld.recreateIsland(island, targetBiome, islandSize, player);

            if (!success) {
                player.sendMessage(Messages.get("error.ONGOING_QUEUE_EVENT"));
                return;
            }

            economy.pay(player, cost);

            player.sendTitle(Messages.get("success.ISLAND_GEN_TITLE"), Messages.get("success.ISLAND_GEN_SUBTITLE"), 10, 20 * 7, 10);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));
        }
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
}
