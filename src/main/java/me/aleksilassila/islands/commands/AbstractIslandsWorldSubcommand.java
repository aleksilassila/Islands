package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIslandsWorldSubcommand extends Subcommand {
    protected abstract void runCommand(Player player, String[] args, boolean confirmed, IslandsConfig.Entry island);

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!player.getWorld().equals(Islands.islandsWorld)) {
            Messages.send(player, "error.WRONG_WORLD");
            return;
        }

        IslandsConfig.Entry island = IslandsConfig.getEntry(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), true);

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
