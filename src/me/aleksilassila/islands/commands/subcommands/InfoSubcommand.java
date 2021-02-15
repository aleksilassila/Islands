package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class InfoSubcommand extends AbstractIslandsWorldSubcommand {
    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, IslandsConfig.Entry island) {
        boolean extensiveInfo = player.hasPermission(Permissions.bypass.info);

        if (!extensiveInfo && !island.isPublic) {
            Messages.send(player, "error.PRIVATE_ISLAND");
            return;
        }

        Messages.send(player, island.isPublic ? "info.ISLAND_INFO_PUBLIC_NAME" : "info.ISLAND_INFO_PRIVATE_NAME", island.name);
        Messages.send(player, "info.ISLAND_INFO_SEPARATOR");

        if (island.uuid == null) {
            Messages.send(player, "info.ISLAND_INFO_OWNER", "Server");
        } else {
            String displayName = Islands.instance.getServer().getOfflinePlayer(island.uuid).getName();
            if (displayName != null) {
                if (extensiveInfo) {
                    Messages.send(player, "info.ISLAND_INFO_OWNER_WITH_UUID", displayName, island.uuid.toString());
                } else {
                    Messages.send(player, "info.ISLAND_INFO_OWNER", displayName);
                }
            }
        }

        Messages.send(player, "info.ISLAND_INFO_SPAWNPOINT", island.spawnPosition[0], island.spawnPosition[1]);
        Messages.send(player, "info.ISLAND_INFO_SIZE", island.size, Islands.instance.parseIslandSize(island.size));
        Messages.send(player, "info.ISLAND_INFO_BIOME", island.biome);

        if (extensiveInfo) {
            Messages.send(player, "info.ISLAND_INFO_HOME", island.homeId);

//            if (island.contains("trusted")) {
//                Messages.send(player, "info.ISLAND_INFO_TRUSTED");
//
//                for (String trustedUUID : island.getConfigurationSection("trusted").getKeys(false)) {
//                    String trustedPlayer;
//                    try {
//                        trustedPlayer = Islands.instance.getServer().getOfflinePlayer(UUID.fromString(trustedUUID)).getName();
//                    } catch (Exception e) {
//                        trustedPlayer = "\u00a74Unknown";
//                    }
//
//                    Messages.send(player, "info.ISLAND_INFO_TRUSTED_PLAYER", trustedPlayer, trustedUUID);
//                }
//            } else {
//                Messages.send(player, "info.ISLAND_INFO_NO_TRUSTED");
//            }

        }
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String help() {
        return "Command that shows info about current island.";
    }

    @Override
    public String getPermission() {
        return Permissions.command.info;
    }
}
