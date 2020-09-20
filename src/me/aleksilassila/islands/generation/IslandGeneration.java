package me.aleksilassila.islands.generation;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.biomes.Biomes;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

import java.util.List;
import java.util.Random;

public class IslandGeneration {

    private Islands islands;
    public Biomes biomes;

    public IslandGeneration(Islands islands) {
        this.islands = islands;
        this.biomes = new Biomes(islands.sourceWorld, 32);
    }

    public boolean isBlockInShape(int x, int y, int z, int islandSize) {
        if ((Math.pow(x - islandSize / 2, 2) + Math.pow(1.2 * y - islandSize / 2, 2) + Math.pow(z - islandSize / 2, 2)) <= Math.pow(islandSize / 2, 2)) {
            return true;
        }

        return false;
    }

    public boolean copyIsland(Biome biome, int islandSize, int targetX, int targetY, int targetZ) {
        List<Location> locations = biomes.availableLocations.get(biome);

        if (locations == null) {
            return false;
        }

        if (locations.size() == 0) {
            return false;
        }

        Location sourceLocation = locations.get(new Random().nextInt(locations.size()));

        int centerY = 100;
        while (true) {
            int centerX = (int) (sourceLocation.getBlockX() + ((double) islandSize) / 2.0);
            int centerZ = (int) (sourceLocation.getBlockZ() + ((double) islandSize) / 2.0);

            Material material = islands.sourceWorld.getBlockAt(centerX, centerY, centerZ).getBlockData().getMaterial();
            if (material == Material.GRASS_BLOCK || material == Material.STONE || material == Material.DIRT || material == Material.SAND || material == Material.SANDSTONE ) {
                break;
            }
            centerY--;
        }

        CuboidRegion sourceRegion = new CuboidRegion(
                new BukkitWorld(islands.sourceWorld),
                BlockVector3.at(
                        sourceLocation.getBlockX(),
                        centerY + islandSize / 2,
                        sourceLocation.getBlockZ()
                ),
                BlockVector3.at(
                        sourceLocation.getBlockX() + islandSize,
                        centerY - islandSize / 2,
                        sourceLocation.getBlockZ() + islandSize
                )
        );

        int startX = sourceLocation.getBlockX();
        int startY = centerY - islandSize / 2;
        int startZ = sourceLocation.getBlockZ();


        Bukkit.getLogger().info("Started copying to (" + targetX + ", " + targetY + ", " + targetZ + ")");
        for (BlockVector3 point : sourceRegion) {
            BlockData sourceData = islands.sourceWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).getBlockData();

            // Relative coordinates: first block (0,0,0), second (1,0,0) etc.
            int relativeX = point.getBlockX() - startX;
            int relativeY = point.getBlockY() - startY;
            int relativeZ = point.getBlockZ() - startZ;

            if ((Math.pow(relativeX - islandSize / 2, 2) + Math.pow(1.2 * relativeY - islandSize / 2, 2) + Math.pow(relativeZ - islandSize / 2, 2)) <= Math.pow(islandSize / 2, 2)){
                islands.world.getBlockAt(targetX + relativeX, targetY + relativeY, targetZ + relativeZ).setBlockData(sourceData);
            } else {
                islands.world.getBlockAt(targetX + relativeX, targetY + relativeY, targetZ + relativeZ).setType(Material.AIR);
            }

            islands.world.getBlockAt(targetX + relativeX, targetY + relativeY, targetZ + relativeZ).setBiome(biome);
        }

        // Update lighting
        islands.world.getChunkAt(targetX + islandSize / 2, targetZ + islandSize / 2);

        return true;
    }
}
