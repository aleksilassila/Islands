package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class ClearSubcommand extends AbstractIslandsWorldSubcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public ClearSubcommand() {
        this.plugin = Islands.instance;
        this.layout = plugin.layout;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (args.length != 0) {
            Messages.send(player, "usage.CLEAR");
            return;
        }

        if (!ownsIsland(player, islandId) && !player.hasPermission(Permissions.bypass.clear)) {
            Messages.send(player, "error.UNAUTHORIZED");
            return;
        }

        if (!confirmed) {
            Messages.send(player, "info.CLEAR_CONFIRM");
            return;
        }

        if (!IslandGeneration.INSTANCE.clearIsland(player, islandId)) {
            player.sendMessage(Messages.get("error.ONGOING_QUEUE_EVENT"));
            return;
        }

        layout.deleteIsland(islandId);
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
