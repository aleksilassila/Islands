package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractCreateSubcommands extends Subcommand {
    public AbstractCreateSubcommands(Islands islands) {
        super(islands);
    }

    protected abstract void openGui(Player player);

    abstract protected void runCommand(Player player, String[] args, boolean confirmed, int islandSize);

    protected boolean isRandomBiomeDisabled() {
        return Plugin.instance.getConfig().getBoolean("disableRandomBiome");
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (args.length == 0) {
            openGui(player);
            return;
        }

        int islandSize = args.length == 2 ? Plugin.instance.parseIslandSize(args[1]) : Plugin.instance.parseIslandSize("");

        if (!player.hasPermission(Plugin.instance.getCreatePermission(islandSize)) && !player.hasPermission(Permissions.command.createAny)) {
            player.sendMessage(Messages.get("error.NO_PERMISSION"));
            return;
        }

        if (islandSize < Plugin.instance.getSmallestIslandSize() || islandSize + 4 >= islands.config.islandSpacing) {
            player.sendMessage(Messages.get("error.INVALID_ISLAND_SIZE"));
            return;
        }

        runCommand(player, args, confirmed, islandSize);
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        List<String> availableArgs = new ArrayList<>();

        if (args.length == 1) {
            HashMap<Biome, List<Location>> availableLocations = islands.sourceWorld.getAvailableLocations();

            if (!isRandomBiomeDisabled())
                availableArgs.add("RANDOM");

            for (Biome biome : availableLocations.keySet()) {
                availableArgs.add(biome.name());
            }

        } else if (args.length == 2) {
            for (String size : Plugin.instance.definedIslandSizes.keySet()) {
                if (player.hasPermission(Plugin.instance.getCreatePermission(Plugin.instance.definedIslandSizes.get(size))) || player.hasPermission(Permissions.command.createAny))
                    availableArgs.add(size);
            }

            availableArgs.sort(
                    Comparator.comparingInt(key -> Plugin.instance.definedIslandSizes.get(key))
            );
        }

        return availableArgs;
    }
}
