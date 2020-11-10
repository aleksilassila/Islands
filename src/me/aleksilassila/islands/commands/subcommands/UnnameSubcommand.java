package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class UnnameSubcommand extends AbstractIslandsWorldSubcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public UnnameSubcommand() {
        this.plugin = Islands.instance;
        this.layout = plugin.layout;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (args.length != 0) {
            Messages.send(player, "usage.UNNAME");
            return;
        }

        if (!plugin.getIslandsConfig().contains(islandId + ".home")
                || !plugin.getIslandsConfig().contains(islandId + ".UUID")) {
            player.sendMessage(Messages.get("error.ISLAND_NO_OWNER"));
            return;
        }

        if (ownsIsland(player, islandId) || player.hasPermission(Permissions.bypass.unname)) {
            layout.unnameIsland(islandId);
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
