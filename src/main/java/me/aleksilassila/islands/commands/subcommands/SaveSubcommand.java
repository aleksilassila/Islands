package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Entry;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
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

    public SaveSubcommand(Islands islands) {
        super(islands);
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, Entry island) {
        if (Plugin.instance.worldEdit != null) {
            String name = island.name;

            int startX = islands.islandsConfig.getConfig().getInt(island + ".x");
            int startY = islands.islandsConfig.getConfig().getInt(island + ".y");
            int startZ = islands.islandsConfig.getConfig().getInt(island + ".z");

            int islandSize = islands.islandsConfig.getConfig().getInt(island + ".size");

            int height;

            try {
                height = args.length == 1
                        ? Integer.parseInt(args[0])
                        : islands.islandsConfig.getConfig().getInt(island + ".height");
            } catch (NumberFormatException e) {
                height = islands.islandsConfig.getConfig().getInt(island + ".height");
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

            if (SaveHandler.saveSchematic(file, islands.islandsWorld.getWorld(), startX, startY, startZ, islandSize, height))
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
