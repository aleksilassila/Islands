package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class InfoSubcommand extends AbstractIslandsWorldSubcommand {
    private final Islands plugin;

    public InfoSubcommand(Islands plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Islands getPlugin() {
        return plugin;
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        ConfigurationSection island = plugin.getIslandsConfig().getConfigurationSection(islandId);

        if (island == null) {
            Messages.send(player, "error.ISLAND_NOT_FOUND");
            return;
        }

        boolean isPublic = island.getBoolean("public");
        String name = isPublic ? island.getString("name") : islandId;
        boolean extensiveInfo = player.hasPermission(Permissions.bypass.info);

        if (!extensiveInfo && !isPublic) {
            Messages.send(player, "error.PRIVATE_ISLAND");
            return;
        }

        Messages.send(player, isPublic ? "info.ISLAND_INFO_PUBLIC_NAME" : "info.ISLAND_INFO_PRIVATE_NAME", name);
        Messages.send(player, "info.ISLAND_INFO_SEPARATOR");

        String ownerUUID = island.getString("UUID");

        if (ownerUUID == null) {
            Messages.send(player, "info.ISLAND_INFO_OWNER", "Server");
        } else {
            String displayName = plugin.getServer().getOfflinePlayer(UUID.fromString(ownerUUID)).getName();
            if (displayName != null) {
                if (extensiveInfo) {
                    Messages.send(player, "info.ISLAND_INFO_OWNER_WITH_UUID", displayName, ownerUUID);
                } else {
                    Messages.send(player, "info.ISLAND_INFO_OWNER", displayName);
                }
            }
        }

        Messages.send(player, "info.ISLAND_INFO_SPAWNPOINT", island.getInt("spawnPoint.x"), island.getInt("spawnPoint.z"));
        Messages.send(player, "info.ISLAND_INFO_SIZE", island.getInt("size"), plugin.parseIslandSize(island.getInt("size")));
        Messages.send(player, "info.ISLAND_INFO_BIOME", Optional.ofNullable(island.getString("biome")).orElse("UNDEFINED"));

        if (extensiveInfo) {
            Messages.send(player, "info.ISLAND_INFO_HOME", island.getInt("home"));

            if (island.getStringList("trusted").size() != 0) {
                Messages.send(player, "info.ISLAND_INFO_TRUSTED");

                for (String trustedUUID : island.getStringList("trusted")) {
                    String trustedPlayer;
                    try {
                        trustedPlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(trustedUUID)).getName();
                    } catch (Exception e) {
                        trustedPlayer = "\u00a74Unknown";
                    }

                    Messages.send(player, "info.ISLAND_INFO_TRUSTED_PLAYER", trustedPlayer, trustedUUID);
                }
            } else {
                Messages.send(player, "info.ISLAND_INFO_NO_TRUSTED");
            }

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

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
