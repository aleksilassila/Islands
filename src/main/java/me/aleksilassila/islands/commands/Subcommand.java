package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Subcommand {
    protected final Islands islands;
    protected final Plugin plugin;

    protected final World islandsWorld;
    @Nullable
    protected final World wildernessWorld;
    protected final World sourceWorld;

    public Subcommand(Islands islands) {
        this.islands = islands;
        this.plugin = islands.plugin;

        this.islandsWorld = islands.getIslandsWorld();
        this.wildernessWorld = islands.getWildernessWorld();
        this.sourceWorld = islands.getSourceWorld();
    }

    public abstract void onCommand(Player player, String[] args, boolean confirmed);

    public abstract List<String> onTabComplete(Player player, String[] args);

    public abstract String getName();

    public abstract String help();

    public abstract String getPermission();
}
