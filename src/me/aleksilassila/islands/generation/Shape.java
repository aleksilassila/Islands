package me.aleksilassila.islands.generation;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sun.istack.internal.NotNull;
import me.aleksilassila.islands.Islands;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class Shape {
    public File file;
    private final BlockVector3 dimensions;
    private final Clipboard clipboard;

    static final String SCHEMATIC_DIRECTORY =  "plugins/Islands/shapes/";

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
        return Math.max(dimensions.getBlockX(), dimensions.getBlockZ());
    }

    public int getHeight() {
        return dimensions.getBlockY() + 1;
    }

    @NotNull
    public static Map<Integer, List<Shape>> loadAllShapes() {
        if (Islands.instance.worldEdit == null) return new HashMap<>();

        File schematicsDirectory = new File(SCHEMATIC_DIRECTORY);
        if (!schematicsDirectory.exists()) schematicsDirectory.mkdirs();

        Map<Integer, List<Shape>> shapes = new HashMap<>();

        for (String file : schematicsDirectory.list()) {
            if (!file.endsWith(".schem") && !file.endsWith(".schematic")) continue;

            try {
                Shape shape = new Shape(new File(SCHEMATIC_DIRECTORY + file));

                if (shapes.containsKey(shape.getWidth()))
                    shapes.get(shape.getWidth()).add(shape);
                else {
                    List<Shape> list = new ArrayList<>(Collections.singletonList(shape));
                    shapes.put(shape.getWidth(), list);
                }

                Islands.instance.getLogger().info("Added shape " + shape.file.getName() + " for islandSize " + shape.getWidth() + ".");

            } catch (IllegalArgumentException ignored) { }
        }

        return shapes;
    }
}
