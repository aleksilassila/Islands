package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

import java.util.List;

public class ClearSubcommand extends Subcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public ClearSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(plugin.islandsWorld)) {
            Messages.send(player, "error.WRONG_WORLD");
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            Messages.send(player, "error.NOT_ON_ISLAND");
            return;
        }

        if (!layout.getUUID(islandId).equals(player.getUniqueId().toString())
                && !player.hasPermission(Permissions.bypass.clear)) {
            Messages.send(player, "error.UNAUTHORIZED");
            return;
        }

        if (!confirmed) {
            Messages.send(player, "info.CLEAR_CONFIRM");
            return;
        }

        if (!plugin.islandGeneration.clearIsland(player, Integer.parseInt(islandId.split("x")[0]), Integer.parseInt(islandId.split("x")[1]))) {
            player.sendMessage(Messages.get("error.ONGOING_QUEUE_EVENT"));
            return;
        }

        layout.deleteIsland(islandId);
        Messages.send(player, "success.DELETED");
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String help() {
        return "Completely clear island and delete it from config.";
    }

    @Override
    public String getPermission() {
        return Permissions.command.delete;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
