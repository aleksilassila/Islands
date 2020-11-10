package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class SetSpawnSubcommand extends AbstractIslandsWorldSubcommand {
    public SetSpawnSubcommand() {}

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (ownsIsland(player, islandId) || player.hasPermission(Permissions.bypass.setSpawn)) {
            Islands.instance.layout.setSpawnPoint(islandId, player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            player.sendMessage(Messages.get("success.SPAWN_POINT_CHANGED"));
        } else {
            player.sendMessage(Messages.get("error.UNAUTHORIZED"));
        }
    }

    @Override
    public String getName() {
        return "setspawn";
    }

    @Override
    public String help() {
        return "Sets island spawn point";
    }

    @Override
    public String getPermission() {
        return Permissions.command.setSpawn;
    }
}
