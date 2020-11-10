package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NameSubcommand extends AbstractIslandsWorldSubcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public NameSubcommand() {
        this.plugin = Islands.instance;
        this.layout = plugin.layout;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (args.length != 1) {
            Messages.send(player, "usage.NAME");
            return;
        }

        if (ownsIsland(player, islandId) || player.hasPermission(Permissions.bypass.name)) {
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
            return new ArrayList<String>(Collections.singletonList("<name>"));
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
}
