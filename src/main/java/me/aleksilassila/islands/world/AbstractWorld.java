package me.aleksilassila.islands.world;

import me.aleksilassila.islands.Config;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
import me.aleksilassila.islands.utils.Utils;
import org.bukkit.World;

abstract public class AbstractWorld {
    protected final Plugin plugin;
    protected final Islands islands;

    protected final Config config;

    World world;

    public AbstractWorld(Islands islands) {
        this.plugin = islands.plugin;
        this.islands = islands;
        System.out.println(islands.config);
        this.config = islands.config;
    }

    abstract String getWorldName();

    abstract World createWorld(boolean exists);

    public World getWorld() {
        if (world == null) {
            boolean exists = Utils.worldExists("islandsSource");
            world = createWorld(exists);
        }

        return world;
    }
}

