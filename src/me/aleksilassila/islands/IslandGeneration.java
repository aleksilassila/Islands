package me.aleksilassila.islands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;

public class IslandGeneration {

    World islandsWorld;
    Biome[][] biomeMap;

    final int indexSize = 2500;
    final int biomeJump = 8;
    final int pointsInBiomeMap = (int) Math.floor(indexSize / biomeJump);

    final int mapWidth = 1;

    public IslandGeneration() {
        this.islandsWorld = createIslandsWorldIfNecessary();
        this.biomeMap = generateBiomeMap();
    }

    World createIslandsWorldIfNecessary() {
        Bukkit.getServer().broadcastMessage("Creating islands world...");
        Bukkit.getServer().getLogger().info("Creating islands world...");

        WorldCreator wc = new WorldCreator("islands");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.NORMAL);

        return wc.createWorld();
    }

    Biome[][] generateBiomeMap() {
        Biome[][] biomeMap = new Biome[pointsInBiomeMap * mapWidth][pointsInBiomeMap];

        for(int index = 0; index < mapWidth; index++) {
            Bukkit.getServer().getLogger().info("Current index: "+index);

            for (int x = 0; x < pointsInBiomeMap; x ++) {
                Bukkit.getServer().getLogger().info("Done: " + Math.round(((double) x / (double)pointsInBiomeMap) * 100) + "%, " + x * pointsInBiomeMap + " points calculated.");
                for (int z = 0; z < pointsInBiomeMap; z ++) {
                    biomeMap[x][z] = islandsWorld.getBiome(
                            indexSize * index + x * biomeJump,
                            180,
                            z * biomeJump
                    );
                }
            }
        }

        return biomeMap;
    }


    boolean isRectInsideBiome(int xCorner, int zCorner, int rectSize, Biome biome) {
        for (int x = 0; x < rectSize; x++) {
            for (int z = 0; z < rectSize; z++) {
                if (biomeMap[xCorner + x][zCorner + z] != biome) {
                    return false;
                }
            }
        }

        Bukkit.getServer().getLogger().info("Biomemap[" + xCorner + "][" + zCorner + "] equals " + biomeMap[xCorner][zCorner]);

        return true;
    }

    public int[] getIslandSourceLocation(Biome biome, int islandSize) {
        int jump = 1;
        int islandX = -1;
        int islandZ = -1;

        int rectSize = islandSize / 8; // * 8

        loop:
        for (int x = 0; x < biomeMap.length - rectSize; x += jump) {
            for (int z = 0; z < biomeMap[0].length - rectSize; z += jump) {
                if (isRectInsideBiome(x, z, rectSize, biome)) {
                    islandX = x * biomeJump;
                    islandZ = z * biomeJump;

                    break loop;
                }
            }
        }

        return new int[]{islandX, islandZ};
    }
}
