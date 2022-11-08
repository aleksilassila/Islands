package me.aleksilassila.islands.world;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;

public class WildernessWorld extends AbstractWorld {
    public HashMap<Player, Location> wildernessPositions = new HashMap<>();

    public WildernessWorld(Islands islands) {
        super(islands);
    }

    @Override
    public World getWorld() {
        if (Plugin.islands.config.disableWilderness) return null;
        return super.getWorld();
    }

    @Override
    String getWorldName() {
        return Optional.ofNullable(plugin.getConfig().getString("wildernessWorldName")).orElse("wilderness");
    }

    @Override
    World createWorld(boolean exists) {
        World world = new WorldCreator(getWorldName()).createWorld();

        if (exists) {
            plugin.getLogger().info("Wilderness world set to " + getWorldName());
        } else {
            plugin.getLogger().info("No wilderness found. Creating one called " + getWorldName() + "...");
            world.setDifficulty(Difficulty.HARD);
        }

        return world;
    }
}
