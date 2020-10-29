package me.aleksilassila.islands.commands.subcommands;

import com.mojang.brigadier.Message;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InfoSubcommand extends Subcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public InfoSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(plugin.islandsWorld)) {
            Messages.send(player, "error.WRONG_WORLD");
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            Messages.send(player, "error.NOT_ON_ISLAND");
            return;
        }

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

        try {
            String displayName = plugin.getServer().getOfflinePlayer(UUID.fromString(island.getString("UUID"))).getName();

            if (extensiveInfo) {
                Messages.send(player, "info.ISLAND_INFO_OWNER_WITH_UUID", displayName, island.getString("UUID"));
            } else {
                Messages.send(player, "info.ISLAND_INFO_OWNER", displayName);
            }
        } catch (Exception ignored) {}

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
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
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
