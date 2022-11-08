package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.Entry;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.AbstractIslandsWorldSubcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

public class UnnameSubcommand extends AbstractIslandsWorldSubcommand {
    public UnnameSubcommand(Islands islands) {
        super(islands);
    }

    @Override
    protected void runCommand(Player player, String[] args, boolean confirmed, Entry island) {
        if (args.length != 0) {
            Messages.send(player, "usage.UNNAME");
            return;
        }

        if (island.uuid == null) {
            player.sendMessage(Messages.get("error.ISLAND_NO_OWNER"));
            return;
        }

        if (player.getUniqueId().equals(island.uuid) || player.hasPermission(Permissions.bypass.unname)) {
            island.unnameIsland();
            Messages.send(player, "success.UNNAMED");
        } else {
            player.sendMessage(Messages.get("error.UNAUTHORIZED"));
        }
    }

    @Override
    public String getName() {
        return "unname";
    }

    @Override
    public String help() {
        return "Unname island and make it private";
    }

    @Override
    public String getPermission() {
        return Permissions.command.unname;
    }
}
