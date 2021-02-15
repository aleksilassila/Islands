package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class ClearSubcommand extends AbstractIslandsWorldSubcommand {
    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, IslandsConfig.Entry island) {
        if (args.length != 0) {
            Messages.send(player, "usage.CLEAR");
            return;
        }

        if (!player.getUniqueId().equals(island.uuid) && !player.hasPermission(Permissions.bypass.clear)) {
            Messages.send(player, "error.UNAUTHORIZED");
            return;
        }

        if (!confirmed) {
            Messages.send(player, "info.CLEAR_CONFIRM");
            return;
        }

        if (!IslandGeneration.INSTANCE.clearIsland(player, island.islandId)) {
            player.sendMessage(Messages.get("error.ONGOING_QUEUE_EVENT"));
            return;
        }

        island.delete();
        Messages.send(player, "success.DELETED");
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
        return Permissions.command.clear;
    }
}
