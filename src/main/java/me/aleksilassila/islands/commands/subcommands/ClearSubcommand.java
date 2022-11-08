package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Entry;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class ClearSubcommand extends AbstractIslandsWorldSubcommand {
    public ClearSubcommand(Islands islands) {
        super(islands);
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, Entry island) {
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

        if (!islands.generator.clearIsland(player, island)) {
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
