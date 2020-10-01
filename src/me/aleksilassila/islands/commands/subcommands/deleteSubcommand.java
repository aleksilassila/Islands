package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.Permissions;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

import java.util.List;

public class deleteSubcommand extends Subcommand {
    private Main plugin;
    private IslandGrid grid;

    public deleteSubcommand(Main plugin) {
        this.plugin = plugin;
        this.grid = plugin.islands.grid;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!Permissions.checkPermission(player, Permissions.command.delete)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        String islandId = grid.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (!plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())
                && !Permissions.checkPermission(player, Permissions.bypass.delete)) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
        }

        if (!confirmed) {
            player.sendMessage(Messages.info.CONFIRM);
            return;
        }

        grid.deleteIsland(islandId);
        player.sendMessage(Messages.success.DELETED);
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String help() {
        return Messages.help.DELETE;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
