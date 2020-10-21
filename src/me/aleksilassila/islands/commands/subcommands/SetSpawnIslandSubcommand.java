package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

import java.util.List;

public class SetSpawnIslandSubcommand extends Subcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public SetSpawnIslandSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.NOT_ON_ISLAND);
            return;
        }

        if (!layout.setSpawnIsland(islandId))
            player.sendMessage(Messages.error.NOT_ON_ISLAND);
        else
            player.sendMessage(Messages.success.SPAWN_ISLAND_CHANGED);
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
    }

    @Override
    public String getName() {
        return "makespawnisland";
    }

    @Override
    public String help() {
        return "Sets island as default respawn island.";
    }

    @Override
    public String getPermission() {
        return Permissions.command.makeSpawnIsland;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
