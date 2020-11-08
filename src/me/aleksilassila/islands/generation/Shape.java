package me.aleksilassila.islands.generation;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;

import java.io.File;
import java.io.FileInputStream;

public class Shape {
    public File file;
    private final BlockVector3 dimensions;
    private final Clipboard clipboard;

    public Shape(File file) throws IllegalArgumentException {
        this.file = file;

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            this.clipboard = reader.read();
            this.dimensions = clipboard.getDimensions();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not load file.");
        }
    }

    public boolean isBlockAir(int x, int y, int z) {
        return clipboard.getBlock(clipboard.getRegion().getMinimumPoint().add(BlockVector3.at(x - 1, y - 1, z - 1))).getBlockType().getMaterial().isAir();
    }

    public int getWidth() {
        return Math.max(dimensions.getBlockX() + 1, dimensions.getBlockZ() + 1);
    }

    public int getHeight() {
        return dimensions.getBlockY() + 1;
    }
}
