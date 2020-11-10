package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.GUIs.CreateGUI;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractCreateSubcommands;
import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.generation.Biomes;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class CreateSubcommand extends AbstractCreateSubcommands {
    private final Islands plugin;
    private final IslandLayout layout;
    private final IslandCommands.Utils utils = new IslandCommands.Utils();

    public CreateSubcommand() {
        this.plugin = Islands.instance;
        this.layout = plugin.layout;
    }

    @Override
    protected void openGui(Player player) {
            new CreateGUI(plugin, player, "create").open();
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, int islandSize) {
        if (args.length > 2) {
            Messages.send(player, "usage.CREATE");
            return;
        }

        HashMap<Biome, List<Location>> availableLocations = Biomes.INSTANCE.availableLocations;

        int previousIslands = layout.getIslandIds(player.getUniqueId()).size();

        int islandsLimit = plugin.getConfig().getInt("defaultIslandLimit", -1);

        if (plugin.perms != null) {
            for (String group : plugin.perms.getGroups()) {
                if (plugin.perms.playerInGroup(player, group)) {
                    islandsLimit = Math.max(plugin.getConfig().getInt("groupLimits." + group, -1), islandsLimit);
                }
            }
        }

        if (previousIslands >= islandsLimit && !player.hasPermission(Permissions.bypass.create) && islandsLimit != -1) {
            player.sendMessage(Messages.get("error.ISLAND_LIMIT"));
            return;
        }

        if (plugin.econ != null && !hasFunds(player, plugin.islandPrices.getOrDefault(islandSize, 0.0))) {
            player.sendMessage(Messages.get("error.INSUFFICIENT_FUNDS"));
            return;
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


            if (!availableLocations.containsKey(targetBiome)) {
                player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));
                return;
            }
        }

        String islandId;

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
}
