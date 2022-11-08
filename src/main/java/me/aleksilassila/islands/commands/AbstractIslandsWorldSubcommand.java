package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Entry;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIslandsWorldSubcommand extends Subcommand {
    public AbstractIslandsWorldSubcommand(Islands islands) {
        super(islands);
    }

    protected abstract void runCommand(Player player, String[] args, boolean confirmed, Entry island);

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(islands.islandsWorld.getWorld())) {
            Messages.send(player, "error.WRONG_WORLD");
            return;
        }

        Entry island = islands.islandsConfig.getEntry(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), true);

        if (island == null) {
            Messages.send(player, "error.NOT_ON_ISLAND");
            return;
        }

        runCommand(player, args, confirmed, island);
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
