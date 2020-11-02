package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIslandsWorldSubcommand extends Subcommand {
    // save, (recreate)
    protected abstract Islands getPlugin();

    protected abstract void runCommand(Player player, String[] args, boolean confirmed, String islandId);

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(getPlugin().islandsWorld)) {
            Messages.send(player, "error.WRONG_WORLD");
            return;
        }

        String islandId = getPlugin().layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            Messages.send(player, "error.NOT_ON_ISLAND");
            return;
        }

        runCommand(player, args, confirmed, islandId);
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }
}
