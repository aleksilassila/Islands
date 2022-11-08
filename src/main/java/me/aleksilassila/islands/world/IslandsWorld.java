package me.aleksilassila.islands.world;

import me.aleksilassila.islands.Entry;
import me.aleksilassila.islands.Islands;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Optional;

public class IslandsWorld extends AbstractWorld {
    public IslandsWorld(Islands islands) {
        super(islands);
    }

    @Override
    String getWorldName() {
        return Optional.ofNullable(plugin.getConfig().getString("islandsWorldName")).orElse("world");
    }

    @Override
    World createWorld(boolean exists) {
        World world = new WorldCreator(getWorldName()).createWorld();

        if (exists) {
            plugin.getLogger().info("Islands world set to " + getWorldName());
        } else {
            plugin.getLogger().info("No islands world found. Creating one called " + getWorldName() + "...");
            world.setDifficulty(Difficulty.NORMAL);
        }

        return world;
    }

    @Nullable
    public String createNewIsland(Biome biome, int islandSize, Player player) throws IllegalArgumentException {
        // If random biome
        biome = Optional.ofNullable(biome).orElse(islands.sourceWorld.getRandomBiome());

        boolean noShape = false;
        if (plugin.getConfig().contains("excludeShapes", true)
                && plugin.getConfig().getStringList("excludeShapes").contains(biome.name())) {
            noShape = true;
        }

        int height = islandSize;

        Entry island = islands.islandsConfig.createIsland(player.getUniqueId(), islandSize, height, biome);

        try {
            boolean success = islands.generator.copyIsland(player, island, false, noShape, island.size);

            if (!success) {
                island.delete();
                return null;
            }

            return island.islandId;
        } catch (IllegalArgumentException e) {
            island.delete();
            throw new IllegalArgumentException();
        }
    }

    public boolean recreateIsland(Entry island, Biome biome, int islandSize, Player player) throws IllegalArgumentException {
        // If random biome
        biome = Optional.ofNullable(biome).orElse(islands.sourceWorld.getRandomBiome());

        boolean noShape = false;
        if (plugin.getConfig().contains("excludeShapes", true)
                && plugin.getConfig().getStringList("excludeShapes").contains(biome.name())) {
            noShape = true;
        }

        int height = islandSize;

        int oldSize = island.size;
        island.size = islandSize;
        island.height = height;
        island.biome = biome;

        island.resizeClaim(islandSize);

        island.shouldUpdate = true;

        try {
            return islands.generator.copyIsland(player, island, true, noShape, oldSize);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }
}
