package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

import java.util.List;

public class SetSpawnSubcommand extends Subcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public SetSpawnSubcommand(Islands plugin) {
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

        if (layout.getUUID(islandId).equals(player.getUniqueId().toString())
                || player.hasPermission(Permissions.bypass.setSpawn)) {
            layout.setSpawnPoint(islandId, player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            player.sendMessage(Messages.success.SPAWN_POINT_CHANGED);
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
