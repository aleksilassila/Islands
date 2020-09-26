package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.Permissions;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class nameSubcommand extends Subcommand {
    private Main plugin;
    private IslandGrid grid;

    public nameSubcommand(Main plugin) {
        this.plugin = plugin;
        this.grid = plugin.islands.grid;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!Permissions.checkPermission(player, Permissions.island.name)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        if (args.length != 1) {
            player.sendMessage(Messages.help.NAME);
            return;
        }

        String islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                || Permissions.checkPermission(player, Permissions.bypass.name)) {
            if (grid.getPublicIsland(args[0]) != null) {
                player.sendMessage(Messages.error.NAME_TAKEN);
                return;
            }

            if (plugin.getConfig().getStringList("illegalIslandNames").contains(args[0])) {
                player.sendMessage(Messages.error.NAME_BLOCKED);
                return;
            }

            grid.nameIsland(islandId, args[0]);

            player.sendMessage(Messages.success.NAME_CHANGED(args[0]));
        } else {
            player.sendMessage(Messages.error.UNAUTHORIZED);
        }

    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return new ArrayList<String>(Arrays.asList("<name>"));
        }

        return null;
    }

    @Override
    public String getName() {
        return "name";
    }

    @Override
    public String help() {
        return Messages.help.NAME;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
