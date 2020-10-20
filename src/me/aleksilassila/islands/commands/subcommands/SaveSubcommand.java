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
        if (!player.hasPermission(Permissions.command.save)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        String islandId = plugin.layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.ISLAND_NOT_FOUND);
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
                        : islandSize;
            } catch (NumberFormatException e) {
                height = islandSize;
            }

            File file = new File(SAVE_DIRECTORY + name + ".schem");

            try { // FIXME Document errors
                file.getParentFile().mkdirs();

                if (file.exists()) {
                    player.sendMessage(Messages.error.ISLAND_SAVE_ERROR);
                    return;
                }

                file.createNewFile();
            } catch (IOException e) {
                player.sendMessage(Messages.error.ISLAND_SAVE_ERROR);
                return;
            }

            if (SaveHandler.saveSchematic(file, plugin.islandsWorld, startX, startY, startZ, islandSize, height))
                player.sendMessage(Messages.success.ISLAND_SAVED(name));
            else
                player.sendMessage(Messages.error.ISLAND_SAVE_ERROR);

        } else {
            player.sendMessage(Messages.error.NO_WORLDEDIT);
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
        return null;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
