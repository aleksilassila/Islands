package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.GUIs.AdminGUI;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ModerateSubcommand extends Subcommand {
    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (args.length < 2) {
            new AdminGUI(player).open();
            return;
        }

        if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
            IslandsConfig.Entry e = IslandsConfig.entries.get(args[1]);

            if (e != null) {
                player.teleport(e.getIslandSpawn());
            } else {
                player.sendMessage(Messages.get("error.ISLAND_NOT_FOUND"));
            }
        } else if (args[0].equalsIgnoreCase("player")) {
            OfflinePlayer targetPlayer = Utils.getOfflinePlayer(args[1]);
            if (targetPlayer != null)
                new AdminGUI(player).showPlayerIslandsGui(targetPlayer.getUniqueId());
            else
                Messages.send(player, "error.PLAYER_NOT_FOUND");
        }
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        List<String> availableArgs = new ArrayList<>();

        if (args.length == 1) {
            availableArgs.add("teleport");
            availableArgs.add("player");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
                availableArgs.add("<IslandId>");
            } else if (args[0].equalsIgnoreCase("player")) {
                for (Player p : Islands.instance.getServer().getOnlinePlayers()) {
                    availableArgs.add(p.getDisplayName());
                }
            }
        }

        return availableArgs;
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
}
