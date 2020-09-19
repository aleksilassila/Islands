package me.aleksilassila.islands;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.List;

public class IslandGeneration {

    World islandsSourceWorld;
    World islandsWorld;

    int biomeSearchJumpBlocks = 8;
    int biomeSearchSize = 5000;

    public IslandGeneration() {
        this.islandsSourceWorld = createIslandsSourceWorldIfNecessary();
        this.islandsWorld = createIslandsWorldIfNecessary();
    }

    World createIslandsSourceWorldIfNecessary() {
        Bukkit.getServer().getLogger().info("Creating islands source world...");

        WorldCreator wc = new WorldCreator("islandsSource");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.NORMAL);

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.PEACEFUL);

        return world;
    }

    World createIslandsWorldIfNecessary() {
        Bukkit.getServer().getLogger().info("Creating islands world...");

        WorldCreator wc = new WorldCreator("islands");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generator(new EmptyWorldGenerator());

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.PEACEFUL);

        return world;
    }

    public boolean generateIsland(Biome biome, int islandSize) {
        List<Location> locations = getAllIslandLocations(islandSize, biome);

        if (locations.size() == 0) {
            Bukkit.getServer().getLogger().info("Number of locations: " + locations.size());
            return false;
        }

        Location sourceLocation = locations.get(0);

        CuboidRegion region = new CuboidRegion(
                new BukkitWorld(islandsSourceWorld),
                BlockVector3.at(
                        sourceLocation.getBlockX(),
                        islandsSourceWorld.getHighestBlockYAt(sourceLocation.getBlockX() + islandSize / 2, sourceLocation.getBlockZ()  + islandSize / 2) + islandSize / 2,
                        sourceLocation.getBlockZ()
                ),
                BlockVector3.at(
                        sourceLocation.getBlockX() + islandSize,
                        islandsSourceWorld.getHighestBlockYAt(sourceLocation.getBlockX() + islandSize / 2, sourceLocation.getBlockZ()  + islandSize / 2) - islandSize / 2,
                        sourceLocation.getBlockZ() + islandSize
                )
        );

        for (BlockVector3 point : region) {
            BlockData sourceData = islandsSourceWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).getBlockData();
            islandsWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).setBlockData(sourceData);
            islandsWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).setBiome(biome);
        }

        // Update lighting
        islandsWorld.getChunkAt(sourceLocation).load();

        return true;
    }

    public List<Location> getAllIslandLocations(int islandSize, Biome biome) {
        List<Location> locations = new ArrayList<Location>();
        List<int[]> jumpInThesePositions = new ArrayList<int[]>();

        loop:
        for (int x = 0; x < biomeSearchSize - islandSize; x += biomeSearchJumpBlocks) {
            for (int z = 0; z < biomeSearchSize - islandSize; z += biomeSearchJumpBlocks) {
                boolean jump = false;

                for (int[] pos : jumpInThesePositions) {
                    if (pos[0] <= x && x <= pos[0] + islandSize && pos[1] <= z && z <= pos[1] + islandSize) {
                        z += islandSize;
                        jump = true;
                        break;
                    }
                }

                if (jump) { continue; }

                if (isRectInsideBiome(x, z, islandSize, biome)) {
                    locations.add(new Location(islandsSourceWorld, x, 180, z));
                    Bukkit.getServer().getLogger().info("Added" + x + ", " + z);
                    jumpInThesePositions.add(new int[]{x, z});
                    z += islandSize;

                    if (locations.size() >= 10) {
                        break loop;
                    }
                }
            }
        }

        return locations;
    }

    Biome getBiome(int x, int z) {
        return islandsSourceWorld.getBiome(x, 180, z);
    }


    boolean isRectInsideBiome(int xCorner, int zCorner, int rectSize, Biome biome) {
        for (int x = 0; x < rectSize; x++) {
            for (int z = 0; z < rectSize; z++) {
                if (getBiome(xCorner + x, zCorner + z) != biome) {
                    return false;
                }
            }
        }
        return true;
    }
}
