package me.aleksilassila.islands.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.World;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveHandler {
    public static boolean saveSchematic(File file, World world, int startX, int startY, int startZ, int islandSize, int height) {
        CuboidRegion region = new CuboidRegion(
                new BukkitWorld(world),
                BlockVector3.at(startX, startY, startZ),
                BlockVector3.at(startX + islandSize, startY + height, startZ + islandSize)
        );

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        forwardExtentCopy.setCopyingEntities(false);

        try {
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            return false;
        }

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

}
