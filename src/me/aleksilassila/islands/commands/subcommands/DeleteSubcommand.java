package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

import java.util.List;

public class DeleteSubcommand extends Subcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public DeleteSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.tl("error.WRONG_WORLD"));
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.tl("error.NOT_ON_ISLAND"));
            return;
        }

        if (!layout.getUUID(islandId).equals(player.getUniqueId().toString())
                && !player.hasPermission(Permissions.bypass.delete)) {
            player.sendMessage(Messages.tl("error.UNAUTHORIZED"));
            return;
        }

        if (!confirmed) {
            player.sendMessage(Messages.tl("info.CONFIRM"));
            return;
        }

        layout.deleteIsland(islandId);
        player.sendMessage(Messages.tl("success.DELETED"));
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
        return "Delete island from config";
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
