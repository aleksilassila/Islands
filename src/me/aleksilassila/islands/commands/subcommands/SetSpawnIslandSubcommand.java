package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class SetSpawnIslandSubcommand extends AbstractIslandsWorldSubcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public SetSpawnIslandSubcommand() {
        this.plugin = Islands.instance;
        this.layout = plugin.layout;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        boolean removedSpawn = plugin.getIslandsConfig().getBoolean(islandId + ".isSpawn");

        if (!layout.setSpawnIsland(islandId))
            player.sendMessage(Messages.get("error.NOT_ON_ISLAND"));
        else
            player.sendMessage(Messages.get(removedSpawn ? "success.SPAWN_ISLAND_REMOVED" : "success.SPAWN_ISLAND_CHANGED"));
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
}
