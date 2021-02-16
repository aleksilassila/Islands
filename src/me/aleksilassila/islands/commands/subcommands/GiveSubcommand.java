package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class GiveSubcommand extends AbstractIslandsWorldSubcommand {
    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, IslandsConfig.Entry island) {
        if ((args.length != 1 && !player.hasPermission(Permissions.bypass.give))
                || (player.hasPermission(Permissions.bypass.give) && args.length > 1)) {
            Messages.send(player, "usage.GIVE");
            return;
        }

        UUID previousUUID = island.uuid;

        if ((previousUUID != null && previousUUID.equals(player.getUniqueId()))
                || player.hasPermission(Permissions.bypass.give)) {
            if (island.isPublic) {
                if (!confirmed) {
                    player.sendMessage(Messages.get("info.CONFIRM"));
                    return;
                }

                if (args.length == 1) {
                    OfflinePlayer targetPlayer = Utils.getOfflinePlayer(args[0]);
                    String previousName = previousUUID == null ? "Server" : Bukkit.getOfflinePlayer(previousUUID).getName();

                    if (targetPlayer == null) {
                        player.sendMessage(Messages.get("error.NO_PLAYER_FOUND"));
                        return;
                    }

                    island.giveIsland(targetPlayer);
                    player.sendMessage(Messages.get("success.OWNER_CHANGED", args[0]));

                    Messages.send(targetPlayer, "success.ISLAND_RECEIVED", island.name, previousName);
                } else {
                    island.giveToServer();
                    player.sendMessage(Messages.get("success.OWNER_REMOVED"));
                }
            } else {
                player.sendMessage(Messages.get("error.NOT_PUBLIC"));
            }
        } else {
            player.sendMessage(Messages.get("error.UNAUTHORIZED"));
        }
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String help() {
        return "Transfer island ownership";
    }

    @Override
    public String getPermission() {
        return Permissions.command.give;
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
    }
}
