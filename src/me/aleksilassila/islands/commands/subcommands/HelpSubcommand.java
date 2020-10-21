package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpSubcommand extends Subcommand {
    IslandManagmentCommands commands;

    public HelpSubcommand(IslandManagmentCommands commands) {
        this.commands = commands;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        player.sendMessage(Messages.help.AVAILABLE_COMMANDS);

        for (Subcommand subcommand : commands.subcommands) {
            player.sendMessage(Messages.help.SUBCOMMAND(subcommand));
        }
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String help() {
        return "Shows help about /island subcommands";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
