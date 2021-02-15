package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class SetSpawnIslandSubcommand extends AbstractIslandsWorldSubcommand {
    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, IslandsConfig.Entry island) {
        island.setSpawnIsland();
        player.sendMessage(Messages.get(island.isSpawn ? "success.SPAWN_ISLAND_CHANGED" : "success.SPAWN_ISLAND_REMOVED"));
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
