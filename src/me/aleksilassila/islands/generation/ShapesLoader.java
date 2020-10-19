package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Islands;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

public class ShapesLoader {
    private final Islands plugin;
    private final File schematicsDirectory;

    String SCHEMATIC_DIRECTORY =  "plugins/Islands/shapes/";

    public ShapesLoader(Islands plugin) {
        this.plugin = plugin;

        this.schematicsDirectory = new File(SCHEMATIC_DIRECTORY);
        if (!schematicsDirectory.exists()) schematicsDirectory.mkdirs();
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
