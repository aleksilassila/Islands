package me.aleksilassila.islands.commands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.Islands;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public abstract class Subcommand {
    public abstract void onCommand(Player player, String[] args, boolean confirmed);
    public abstract String getName();
    public abstract String help();
    public abstract String[] aliases();
}
