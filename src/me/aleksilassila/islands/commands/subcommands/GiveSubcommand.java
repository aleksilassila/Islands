package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
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
    private final Islands plugin;
    private final IslandLayout layout;

    public GiveSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    protected Islands getPlugin() {
        return plugin;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if ((args.length != 1 && !player.hasPermission(Permissions.bypass.give))
                || (player.hasPermission(Permissions.bypass.give) && args.length > 1)) {
            player.sendMessage(Messages.help.GIVE);
            return;
        }

        String previousUUID = plugin.getIslandsConfig().getString(islandId + ".UUID");

        if ((previousUUID != null && previousUUID.equals(player.getUniqueId().toString()))
                || player.hasPermission(Permissions.bypass.give)) {
            if (plugin.getIslandsConfig().getBoolean(islandId + ".public")) {
                if (!confirmed) {
                    player.sendMessage(Messages.get("info.CONFIRM"));
                    return;
                }

                if (args.length == 1) {
                    OfflinePlayer targetPlayer = Utils.getOfflinePlayer(args[0]);
                    String previousName = previousUUID == null ? "Server" : Bukkit.getOfflinePlayer(UUID.fromString(previousUUID)).getName();

                    if (targetPlayer == null) {
                        player.sendMessage(Messages.get("error.NO_PLAYER_FOUND"));
                        return;
                    }

                    layout.giveIsland(islandId, targetPlayer);
                    player.sendMessage(Messages.get("success.OWNER_CHANGED", args[0]));

                    Messages.send(targetPlayer, "success.ISLAND_RECEIVED", plugin.getIslandsConfig().getString(islandId + ".name"), previousName);
                } else {
                    layout.giveIsland(islandId);
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

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
