package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.entity.Player;

public abstract class CreationSubcommand extends Subcommand {
    protected abstract Islands getPlugin();

    protected boolean buy(Player player, int islandSize) {
        if (getPlugin().econ == null) return true;
        if (!getPlugin().islandCosts.containsKey(islandSize)) return true;

        double cost = getPlugin().islandCosts.get(islandSize);

        if (getPlugin().econ.has(player, cost)) {
            getPlugin().econ.withdrawPlayer(player, cost);
            player.sendMessage(Messages.get("success.ISLAND_PURCHASED", cost));
            return true;
        } else {
            player.sendMessage(Messages.get("error.INSUFFICIENT_FUNDS"));
            return false;
        }
    }
}
