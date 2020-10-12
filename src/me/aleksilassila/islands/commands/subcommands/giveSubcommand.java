package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class giveSubcommand extends Subcommand {
    private final Main plugin;
    private final IslandLayout layout;

    public giveSubcommand(Main plugin) {
        this.plugin = plugin;
        this.layout = plugin.islands.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!Permissions.checkPermission(player, Permissions.command.give)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        if ((args.length != 1 && !player.hasPermission(Permissions.bypass.give))
                || (player.hasPermission(Permissions.bypass.give) && args.length > 1)) {
            player.sendMessage(Messages.help.GIVE);
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }
        ConfigurationSection section = plugin.getIslandsConfig().getConfigurationSection(islandId + ".UUID");
        if ((section != null && layout.getUUID(islandId).equals(player.getUniqueId().toString()))
                || Permissions.checkPermission(player, Permissions.bypass.give)) {
            if (plugin.getIslandsConfig().getBoolean(islandId + ".public")) {
                if (!confirmed) {
                    player.sendMessage(Messages.info.CONFIRM);
                    return;
                }

                if (args.length == 1) {
                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    if (targetPlayer == null) {
                        player.sendMessage(Messages.error.NO_PLAYER_FOUND);
                        return;
                    }

                    layout.giveIsland(islandId, targetPlayer);
                    player.sendMessage(Messages.success.OWNER_CHANGED(args[0]));
                    targetPlayer.sendMessage(Messages.success.ISLAND_RECEIVED(targetPlayer.getName(), args[0]));
                } else {
                    layout.giveIsland(islandId);
                    player.sendMessage(Messages.success.OWNER_REMOVED);
                }
            } else {
                player.sendMessage(Messages.error.NOT_PUBLIC);
            }
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
        return "give";
    }

    @Override
    public String help() {
        return Messages.help.GIVE;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
