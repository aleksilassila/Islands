package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class UnnameSubcommand extends AbstractIslandsWorldSubcommand {
    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (args.length != 0) {
            Messages.send(player, "usage.UNNAME");
            return;
        }

        if (!IslandsConfig.getConfig().contains(islandId + ".home")
                || !IslandsConfig.getConfig().contains(islandId + ".UUID")) {
            player.sendMessage(Messages.get("error.ISLAND_NO_OWNER"));
            return;
        }

        if (ownsIsland(player, islandId) || player.hasPermission(Permissions.bypass.unname)) {
            IslandsConfig.unnameIsland(islandId);
            Messages.send(player, "success.UNNAMED");
        } else {
            player.sendMessage(Messages.get("error.UNAUTHORIZED"));
        }
    }

    @Override
    public String getName() {
        return "unname";
    }

    @Override
    public String help() {
        return "Unname island and make it private";
    }

    @Override
    public String getPermission() {
        return Permissions.command.unname;
    }
}
