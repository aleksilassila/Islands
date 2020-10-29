package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GenerationSubcommands extends Subcommand {
    abstract Islands getPlugin();

    boolean validateCommand(Player player, int islandSize) {
        if (!player.hasPermission(getPlugin().getCreatePermission(islandSize))) {
            player.sendMessage(Messages.get("error.NO_PERMISSION"));
            return false;
        }

        if (islandSize < getPlugin().getSmallestIslandSize() || islandSize + 4 >= getPlugin().layout.islandSpacing) {
            player.sendMessage(Messages.get("error.INVALID_ISLAND_SIZE"));
            return false;
        }

        return true;
    }

    boolean hasFunds(Player player, double cost) {
        if (getPlugin().econ == null || player.hasPermission(Permissions.bypass.economy)) return true;

        return getPlugin().econ.has(player, cost);
    }

    void pay(Player player, double cost) {
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
