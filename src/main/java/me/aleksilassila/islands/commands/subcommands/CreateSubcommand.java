package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.GUIs.CreateGUI;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractCreateSubcommands;
import me.aleksilassila.islands.plugins.Economy;
import me.aleksilassila.islands.plugins.VaultPermissions;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class CreateSubcommand extends AbstractCreateSubcommands {
    private final Economy economy;
    private final VaultPermissions vaultPermissions;

    public CreateSubcommand(Islands islands) {
        super(islands);
        this.economy = islands.economy;
        this.vaultPermissions = islands.vaultPermissions;
    }

    @Override
    protected void openGui(Player player) {
        new CreateGUI(islands, player, "create").open();
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, int islandSize) {
        if (args.length > 2) {
            Messages.send(player, "usage.CREATE");
            return;
        }

        HashMap<Biome, List<Location>> availableLocations = islands.sourceWorld.getAvailableLocations();

        int previousIslands = islands.islandsConfig.getOwnedIslands(player.getUniqueId()).size();

        int islandsLimit = islands.plugin.getConfig().getInt("defaultIslandLimit", -1);

        if (vaultPermissions.isEnabled()) {
            for (String group : vaultPermissions.getPermissions().getGroups()) {
                if (vaultPermissions.getPermissions().playerInGroup(player, group)) {
                    islandsLimit = Math.max(islands.plugin.getConfig().getInt("groupLimits." + group, -1), islandsLimit);
                }
            }
        }

        if (previousIslands >= islandsLimit && !player.hasPermission(Permissions.bypass.create) && islandsLimit != -1) {
            player.sendMessage(Messages.get("error.ISLAND_LIMIT"));
            return;
        }

        if (!economy.canCreate(player, islandSize)) {
            return;
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


            if (!availableLocations.containsKey(targetBiome)) {
                player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));
                return;
            }
        }

        String islandId;

        try {
            islandId = islands.islandsWorld.createNewIsland(targetBiome, islandSize, player);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Messages.get("error.NO_LOCATIONS_FOR_BIOME"));

            return;
        }

        if (islandId == null) {
            player.sendMessage(Messages.get("error.ONGOING_QUEUE_EVENT"));
            return;
        }

        economy.pay(player, economy.islandPrices.getOrDefault(islandSize, 0.0)); // FIXME maybe purchase(islandSize) instead?
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
