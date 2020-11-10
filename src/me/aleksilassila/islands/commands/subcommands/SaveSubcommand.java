package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.SaveHandler;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SaveSubcommand extends AbstractIslandsWorldSubcommand {
    private final String SAVE_DIRECTORY = "plugins/Islands/saves/";

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, String islandId) {
        if (Islands.instance.worldEdit != null) {
            String name = IslandsConfig.getConfig().getBoolean(islandId + ".public")
                    ? IslandsConfig.getConfig().getString(islandId + ".name")
                    : islandId;

            int startX = IslandsConfig.getConfig().getInt(islandId + ".x");
            int startY = IslandsConfig.getConfig().getInt(islandId + ".y");
            int startZ = IslandsConfig.getConfig().getInt(islandId + ".z");

            int islandSize = IslandsConfig.getConfig().getInt(islandId + ".size");

            int height;

            try {
                height = args.length == 1
                        ? Integer.parseInt(args[0])
                        : IslandsConfig.getConfig().getInt(islandId + ".height");
            } catch (NumberFormatException e) {
                height = IslandsConfig.getConfig().getInt(islandId + ".height");
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

            if (SaveHandler.saveSchematic(file, Islands.islandsWorld, startX, startY, startZ, islandSize, height))
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
}
