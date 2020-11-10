package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.GUIs.IslandSettingsGUI;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class SettingsSubcommand extends AbstractIslandsWorldSubcommand {
    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (!ownsIsland(player, islandId) && !player.hasPermission(Permissions.bypass.settings)) {
            Messages.send(player, "error.UNAUTHORIZED");
            return;
        }

        new IslandSettingsGUI(islandId, player).open();
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public String getPermission() {
        return Permissions.command.settings;
    }
}
