package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NameSubcommand extends Subcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public NameSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.get("error.WRONG_WORLD"));
            return;
        }

        if (args.length != 1) {
            player.sendMessage(Messages.help.NAME);
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.get("error.NOT_ON_ISLAND"));
            return;
        }

        if (layout.getUUID(islandId).equals(player.getUniqueId().toString())
                || player.hasPermission(Permissions.bypass.name)) {
            if (layout.getIslandByName(args[0]) != null) {
                player.sendMessage(Messages.get("error.NAME_TAKEN"));
                return;
            }

            if (plugin.getConfig().getStringList("illegalIslandNames").contains(args[0])) {
                player.sendMessage(Messages.get("error.NAME_BLOCKED"));
                return;
            }

            layout.nameIsland(islandId, args[0]);

            player.sendMessage(Messages.get("success.NAME_CHANGED", args[0]));
        } else {
            player.sendMessage(Messages.get("error.UNAUTHORIZED"));
        }

    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return new ArrayList<String>(Arrays.asList("<name>"));
        }

        return null;
    }

    @Override
    public String getName() {
        return "name";
    }

    @Override
    public String help() {
        return "Name island and make it public";
    }

    @Override
    public String getPermission() {
        return Permissions.command.name;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
