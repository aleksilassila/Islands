package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.GUIs.AdminGUI;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

import java.util.List;

public class ModerateSubcommand extends Subcommand {
    private final Islands plugin;

    public ModerateSubcommand(Islands plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        new AdminGUI(plugin, player).open();
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
    }

    @Override
    public String getName() {
        return "moderate";
    }

    @Override
    public String help() {
        return "Admin tools for moderating islands and players";
    }

    @Override
    public String getPermission() {
        return Permissions.command.moderate;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
