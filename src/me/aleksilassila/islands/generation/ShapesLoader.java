package me.aleksilassila.islands.generation;

import com.sun.istack.internal.NotNull;
import me.aleksilassila.islands.Islands;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class ShapesLoader {
    private final Islands plugin;
    private final File schematicsDirectory;

    String SCHEMATIC_DIRECTORY =  "plugins/Islands/shapes/";

    public ShapesLoader(Islands plugin) {
        this.plugin = plugin;

        this.schematicsDirectory = new File(SCHEMATIC_DIRECTORY);
        if (!schematicsDirectory.exists()) schematicsDirectory.mkdirs();
    }

    @NotNull
    public Map<Integer, List<Shape>> loadAll() {
        Map<Integer, List<Shape>> shapes = new HashMap<>();

        for (String file : Objects.requireNonNull(schematicsDirectory.list())) {
            if (!file.endsWith(".schem") && !file.endsWith(".schematic")) continue;

            try {
                Shape shape = new Shape(new File(SCHEMATIC_DIRECTORY + file));

                if (shapes.containsKey(shape.getWidth()))
                    shapes.get(shape.getWidth()).add(shape);
                else {
                    List<Shape> list = new ArrayList<>(Collections.singletonList(shape));
                    shapes.put(shape.getWidth(), list);
                }

                plugin.getLogger().info("Added shape " + shape.file.getName() + " for islandSize " + shape.getWidth() + ".");

            } catch (IllegalArgumentException ignored) { }
        }

        return shapes;
    }

    @Nullable
    public Shape loadFromFile(String fileName) {
        for (String file : Objects.requireNonNull(schematicsDirectory.list())) {
            if (!file.equalsIgnoreCase(fileName + ".schem") && !file.equalsIgnoreCase(fileName + ".schematic") && !file.equalsIgnoreCase(fileName + ".litematic")) continue;

            try {
                return new Shape(new File(SCHEMATIC_DIRECTORY + file));

            } catch (IllegalArgumentException ignored) { }
        }

        plugin.getLogger().severe("Could not load schematic file " + fileName);

        return null;
    }
}
