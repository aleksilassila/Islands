package me.aleksilassila.islands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

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

        return wc.createWorld();
    }

    World createIslandsWorldIfNecessary() {
        Bukkit.getServer().getLogger().info("Creating islands world...");

        WorldCreator wc = new WorldCreator("islands");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generator(new EmptyWorldGenerator());

        return wc.createWorld();
    }

//    public boolean generateIsland(Biome biome, int islandSize) {
//        com.sk89q.worldedit.world.World world = new BukkitWorld(islandsSourceWorld);
//
//        List<Location> locations = getAllIslandLocations(islandSize, biome);
//
//        if (locations.size() == 0) {
//            return false;
//        }
//
//        Location sourceLocation = locations.get(0);
//
//        int high = islandsSourceWorld.getHighestBlockYAt(sourceLocation);
//
//        CuboidRegion region = new CuboidRegion(
//                world,
//                BlockVector3.at(sourceLocation.getBlockX(), high, sourceLocation.getBlockZ()),
//                BlockVector3.at(sourceLocation.getBlockX() + islandSize, high - islandSize, sourceLocation.getBlockZ() + islandSize)
//        );
//
//        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
//
//        try (EditSession editSession = Islands.getWorldEdit().getWorldEdit().getInstance().getEditSessionFactory().getEditSession(world, -1)) {
//            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
//                editSession, region, clipboard, region.getMinimumPoint()
//            );
//            // configure here
//            Operations.complete(forwardExtentCopy);
//        } catch (WorldEditException e) {
//            e.printStackTrace();
//        }
//
//        try (EditSession editSession = Islands.getWorldEdit().getWorldEdit().getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(islandsWorld), -1)) {
//            Operation operation = new ClipboardHolder(clipboard)
//            .createPaste(editSession)
//            .to(BlockVector3.at(0, 100, 0))
//            // configure here
//            .build();
//
//            Operations.complete(operation);
//        } catch (WorldEditException e) {
//            e.printStackTrace();
//        }
//
//        return true;
//
//    }

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
                        islandsSourceWorld.getHighestBlockYAt(sourceLocation.getBlockX(), sourceLocation.getBlockZ()),
                        sourceLocation.getBlockZ()
                ),
                BlockVector3.at(
                        sourceLocation.getBlockX() + islandSize,
                        islandsSourceWorld.getHighestBlockYAt(sourceLocation.getBlockX(), sourceLocation.getBlockZ()) - islandSize,
                        sourceLocation.getBlockZ() + islandSize
                )
        );

        for (BlockVector3 point : region) {
            Bukkit.getServer().getLogger().info("Copying coordinate" + point.getBlockX() + ", " + point.getBlockY() + ", " + point.getBlockZ());
            BlockData sourceData = islandsSourceWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).getBlockData();
            islandsWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).setBlockData(sourceData);
        }

        return true;
    }

    public void copyRegion(Location sourceA, Location sourceB, Location destination) {
        CuboidRegion region = new CuboidRegion(
                new BukkitWorld(sourceA.getWorld()),
                BlockVector3.at(sourceA.getBlockX(), sourceA.getBlockY(), sourceA.getBlockZ()),
                BlockVector3.at(sourceB.getBlockX(), sourceB.getBlockY(), sourceB.getBlockZ())
        );

        region = new CuboidRegion(
                new BukkitWorld(islandsSourceWorld),
                BlockVector3.at(0, 1, 0),
                BlockVector3.at(10, 10, 10)
        );

        for (BlockVector3 point : region) {
            BlockData sourceData = islandsSourceWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).getBlockData();
            islandsWorld.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ()).setBlockData(sourceData);
        }
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
