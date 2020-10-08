package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

import java.util.List;

public class unnameSubcommand extends Subcommand {
    private final Main plugin;
    private final IslandLayout layout;

    public unnameSubcommand(Main plugin) {
        this.plugin = plugin;
        this.layout = plugin.islands.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!Permissions.checkPermission(player, Permissions.command.unname)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        if (args.length != 0) {
            player.sendMessage(Messages.help.UNNAME);
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getInt(islandId + ".home") <= 0
                || plugin.getIslandsConfig().getString(islandId + ".UUID") == null) {
            player.sendMessage(Messages.error.ISLAND_NO_OWNER);
            return;
        }

        if (plugin.getIslandsConfig().getString(islandId + ".UUID").equals(player.getUniqueId().toString())
                || Permissions.checkPermission(player, Permissions.bypass.unname)) {
            layout.unnameIsland(islandId);

            player.sendMessage(Messages.success.UNNAMED);
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
        return "unname";
    }

    @Override
    public String help() {
        return Messages.help.UNNAME;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
