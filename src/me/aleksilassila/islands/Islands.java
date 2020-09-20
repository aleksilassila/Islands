package me.aleksilassila.islands;

import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.generation.IslandGrid;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.UUID;


public class Islands {
    public Main plugin;
    public World world;
    public World sourceWorld;
    // Island positions

    public IslandGeneration islandGeneration;
    public IslandGrid grid;


    public enum IslandSize {
        SMALL, // 32*32
        NORMAL, // 64*64
        BIG, // 80*80
        HUGE
    }

    public Islands(World world, World sourceWorld, Main plugin) {
        this.plugin = plugin;

        this.world = world;
        this.sourceWorld = sourceWorld;

        this.islandGeneration = new IslandGeneration(this);
        this.grid = new IslandGrid(this);

        Bukkit.getLogger().info("World source seed: " + sourceWorld.getSeed());
    }

    public static class IslandsException extends java.lang.Exception {
        public IslandsException(String message) {
            super(message);
        }
    }

    private int getIslandSize(IslandSize size) {
        switch (size) {
            case SMALL:
                return 32;
            case BIG:
                return 80;
            case NORMAL:
            default:
                return 64;
        }
    }

    public String createNewIsland(Biome biome, IslandSize size, UUID uuid) throws IslandsException {
        int islandSize = getIslandSize(size);

        try {
            String islandId = grid.createIsland(uuid, islandSize);

            boolean success = islandGeneration.copyIsland(
                    biome,
                    islandSize,
                    plugin.getConfig().getInt("islands."+islandId+".x"),
                    plugin.getConfig().getInt("islands."+islandId+".y"),
                    plugin.getConfig().getInt("islands."+islandId+".z")
            );

            if (!success) {
                throw new IslandsException("Could not copy island");
            }

            return islandId;

        } catch (IslandGrid.IslandGridException e) {
            throw new IslandsException(e.getMessage());
        }
    }

    public boolean regenerateIsland(Biome biome, UUID uuid, String name) {
        try {
            String islandId = grid.getIslandId(uuid, name);

            boolean success = islandGeneration.copyIsland(
                    biome,
                    plugin.getConfig().getInt("islands." + islandId + ".size"),
                    plugin.getConfig().getInt("islands."+islandId+".x"),
                    plugin.getConfig().getInt("islands."+islandId+".y"),
                    plugin.getConfig().getInt("islands."+islandId+".z")
            );

            return success;

        } catch (IslandGrid.IslandGridException e) {
            return false;
        }
    };
}
