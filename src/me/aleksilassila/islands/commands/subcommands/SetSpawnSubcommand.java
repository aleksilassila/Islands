package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class SetSpawnSubcommand extends AbstractIslandsWorldSubcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public SetSpawnSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    protected Islands getPlugin() {
        return plugin;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (layout.getUUID(islandId).equals(player.getUniqueId().toString())
                || player.hasPermission(Permissions.bypass.setSpawn)) {
            layout.setSpawnPoint(islandId, player.getLocation().getBlockX(), player.getLocation().getBlockZ());

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

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
