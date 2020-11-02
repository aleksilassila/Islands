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

    public UnnameSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    protected Islands getPlugin() {
        return plugin;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (args.length != 0) {
            player.sendMessage(Messages.help.UNNAME);
            return;
        }

        if (plugin.getIslandsConfig().getInt(islandId + ".home") <= 0
                || plugin.getIslandsConfig().getString(islandId + ".UUID") == null) {
            player.sendMessage(Messages.get("error.ISLAND_NO_OWNER"));
            return;
        }

        if (layout.getUUID(islandId).equals(player.getUniqueId().toString())
                || player.hasPermission(Permissions.bypass.unname)) {
            layout.unnameIsland(islandId);

            player.sendMessage(Messages.get("success.UNNAMED"));
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

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
