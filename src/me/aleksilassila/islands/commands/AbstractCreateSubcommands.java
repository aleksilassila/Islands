package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractCreateSubcommands extends Subcommand {
    protected abstract Islands getPlugin();
    protected abstract void openGui(Player player);
    abstract protected void runCommand(Player player, String[] args, boolean confirmed, int islandSize);

    protected boolean isRandomBiomeDisabled() {
        return getPlugin().getConfig().getBoolean("disableRandomBiome");
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (args.length == 0) {
            openGui(player);
            return;
        }

        int islandSize = args.length == 2 ? getPlugin().parseIslandSize(args[1]) : getPlugin().parseIslandSize("");

        if (!player.hasPermission(getPlugin().getCreatePermission(islandSize)) && !player.hasPermission(Permissions.command.createAny)) { // Fixme test if this was necessary
            player.sendMessage(Messages.get("error.NO_PERMISSION"));
            return;
        }

        if (islandSize < getPlugin().getSmallestIslandSize() || islandSize + 4 >= getPlugin().layout.islandSpacing) {
            player.sendMessage(Messages.get("error.INVALID_ISLAND_SIZE"));
            return;
        }

        runCommand(player, args, confirmed, islandSize);
    }

    protected boolean hasFunds(Player player, double cost) {
        if (getPlugin().econ == null || player.hasPermission(Permissions.bypass.economy)) return true;

        return getPlugin().econ.has(player, cost);
    }

    protected void pay(Player player, double cost) {
        if (getPlugin().econ == null || player.hasPermission(Permissions.bypass.economy)) return;

        if (cost > 0) {
            getPlugin().econ.withdrawPlayer(player, cost);
            player.sendMessage(Messages.get("success.ISLAND_PURCHASED", cost));
        }
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        List<String> availableArgs = new ArrayList<>();

        if (args.length == 1) {
            HashMap<Biome, List<Location>> availableLocations = getPlugin().islandGeneration.biomes.availableLocations;

            if (!isRandomBiomeDisabled())
                availableArgs.add("RANDOM");

            for (Biome biome : availableLocations.keySet()) {
                availableArgs.add(biome.name());
            }

        } else if (args.length == 2) {
            for (String size : getPlugin().definedIslandSizes.keySet()) {
                if (player.hasPermission(getPlugin().getCreatePermission(getPlugin().definedIslandSizes.get(size))))
                    availableArgs.add(size);
            }
        }

        return availableArgs;
    }
}
