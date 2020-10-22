package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.SaveHandler;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SaveSubcommand extends Subcommand {
    private final Islands plugin;
    private final String SAVE_DIRECTORY = "plugins/Islands/saves/";

    public SaveSubcommand(Islands plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.get("error.WRONG_WORLD"));
            return;
        }

        String islandId = plugin.layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.get("error.NOT_ON_ISLAND"));
            return;
        }

        if (plugin.worldEdit != null) {
            String name = plugin.getIslandsConfig().getBoolean(islandId + ".public")
                    ? plugin.getIslandsConfig().getString(islandId + ".name")
                    : islandId;

            int startX = plugin.getIslandsConfig().getInt(islandId + ".x");
            int startY = plugin.getIslandsConfig().getInt(islandId + ".y");
            int startZ = plugin.getIslandsConfig().getInt(islandId + ".z");

            int islandSize = plugin.getIslandsConfig().getInt(islandId + ".size");

            int height;

            try {
                height = args.length == 1
                        ? Integer.parseInt(args[0])
                        : plugin.getIslandsConfig().getInt(islandId + ".height");
            } catch (NumberFormatException e) {
                height = plugin.getIslandsConfig().getInt(islandId + ".height");
            }

            if (height == 0) height = islandSize;

            File file = new File(SAVE_DIRECTORY + name + ".schem");

            try {
                file.getParentFile().mkdirs();

                if (file.exists()) {
                    player.sendMessage(Messages.get("error.ISLAND_SAVE_ERROR", name));
                    return;
                }

                file.createNewFile();
            } catch (IOException e) {
                player.sendMessage(Messages.get("error.ISLAND_SAVE_ERROR", name));
                return;
            }

            if (SaveHandler.saveSchematic(file, plugin.islandsWorld, startX, startY, startZ, islandSize, height))
                player.sendMessage(Messages.get("success.ISLAND_SAVED", name, islandSize, height));
            else
                player.sendMessage(Messages.get("error.ISLAND_SAVE_ERROR", name));

        } else {
            player.sendMessage(Messages.get("error.NO_WORLDEDIT"));
        }
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("<height>");
        }
        return null;
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String help() {
        return "Save island as .schem file";
    }

    @Override
    public String getPermission() {
        return Permissions.command.save;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
