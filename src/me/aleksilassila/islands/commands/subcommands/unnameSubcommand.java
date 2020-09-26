package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.Permissions;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

import java.util.List;

public class unnameSubcommand extends Subcommand {
    private Main plugin;
    private IslandGrid grid;

    public unnameSubcommand(Main plugin) {
        this.plugin = plugin;
        this.grid = plugin.islands.grid;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!Permissions.checkPermission(player, Permissions.island.unname)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        if (args.length != 0) {
            player.sendMessage(Messages.help.UNNAME);
            return;
        }

        String islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                || Permissions.checkPermission(player, Permissions.bypass.unname)) {
            grid.unnameIsland(islandId);

            player.sendMessage(Messages.success.UNNAMED);
        } else {
            player.sendMessage(Messages.error.UNAUTHORIZED);
        }
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
    }

    @Override
    public String getName() {
        return "unname";
    }

    @Override
    public String help() {
        return Messages.help.UNNAME;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
