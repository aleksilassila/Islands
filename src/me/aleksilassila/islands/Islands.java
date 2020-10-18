package me.aleksilassila.islands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.commands.GUIs.VisitGui;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.generation.Shape;
import me.aleksilassila.islands.utils.ConfirmItem;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class Islands {
    public Main plugin;
    public World world;
    public World sourceWorld;

    public IslandGeneration islandGeneration;
    public IslandLayout layout;

    public Set<Player> playersWithNoFall = new HashSet<>();
    public final HashMap<String, ConfirmItem> confirmations;
    public Map<String, Long> teleportCooldowns;

    public final VisitGui visitGui;

    public final Map<String, Integer> definedIslandSizes;
    public Map<Integer, Shape> definedIslandShapes;

    public Islands(World sourceWorld, Main plugin) {
        this.plugin = plugin;
        this.sourceWorld = sourceWorld;
        this.teleportCooldowns = new HashMap<>();
        this.confirmations = new HashMap<>();

        this.definedIslandSizes = setupSizes();
        this.definedIslandShapes = setupShapes();

        this.islandGeneration = new IslandGeneration(this);
        this.layout = new IslandLayout(this);

        this.visitGui = new VisitGui(plugin);
    }

    @Nullable
    public String createNewIsland(Biome biome, int islandSize, Player player) throws IllegalArgumentException {
        String islandId = layout.createIsland(player.getUniqueId(), islandSize, biome);
        try {
            boolean success = islandGeneration.copyIsland(
                    player,
                    biome,
                    islandSize,
                    plugin.getIslandsConfig().getInt(islandId + ".x"),
                    plugin.getIslandsConfig().getInt(islandId + ".y"),
                    plugin.getIslandsConfig().getInt(islandId + ".z"),
                    false,
                    0,
                    0,
                    definedIslandShapes.getOrDefault(islandSize, null)
            );

            if (!success) {
                layout.deleteIsland(islandId);
                return null;
            }

            return islandId;
        } catch (IllegalArgumentException e) {
            layout.deleteIsland(islandId);
            throw new IllegalArgumentException();
        }

    }

    public boolean regenerateIsland(String islandId, Biome biome, int islandSize, Player player, boolean shouldClearArea) throws IllegalArgumentException {
        layout.updateIsland(islandId, islandSize, biome);

        try {

            return islandGeneration.copyIsland(
                    player,
                    biome,
                    islandSize,
                    plugin.getIslandsConfig().getInt(islandId + ".x"),
                    plugin.getIslandsConfig().getInt(islandId + ".y"),
                    plugin.getIslandsConfig().getInt(islandId + ".z"),
                    shouldClearArea,
                    plugin.getIslandsConfig().getInt(islandId + ".xIndex"),
                    plugin.getIslandsConfig().getInt(islandId + ".zIndex"),
                    definedIslandShapes.getOrDefault(islandSize, null)
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }

    @NotNull
    public int parseIslandSize(String size) {
        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedSize.equalsIgnoreCase(size)) return definedIslandSizes.get(definedSize);
        }

        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            return definedIslandSizes.containsKey("NORMAL") ? definedIslandSizes.get("NORMAL") : definedIslandSizes.get(definedIslandSizes.keySet().iterator().next());
        }
    }

    @NotNull
    public int getSmallestIslandSize() {
        int smallestSize = layout.islandSpacing;

        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedIslandSizes.get(definedSize) < smallestSize)
                smallestSize = definedIslandSizes.get(definedSize);
        }

        return smallestSize;
    }

    @NotNull
    public String getCreatePermission(int islandSize) {
        // FIXME: Explain in wiki

        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedIslandSizes.get(definedSize) == islandSize) return Permissions.command.create + "." + definedSize;
        }

        return Permissions.command.createCustom;
    }

    private Map<String, Integer> setupSizes() {
        ConfigurationSection configIslandSizes = plugin.getConfig().getConfigurationSection("islandSizes");
        Map<String, Integer> sizes = new HashMap<>();

        if (configIslandSizes == null) {
            plugin.getLogger().severe("PLEASE DEFINE AT LEAST 1 ISLAND SIZE IN config.yml UNDER islandSizes:");
            plugin.getPluginLoader().disablePlugin(plugin);
            return sizes;
        }


        for (String size : configIslandSizes.getKeys(false)) {
            int parsedSize = plugin.getConfig().getInt("islandSizes." + size);

            if (parsedSize <= 0) {
                plugin.getLogger().severe("Island size " + size + " has to be an integer and bigger than 0. Ignoring " + size + ".");
                continue;
            }

            sizes.put(size.toUpperCase(), parsedSize);
        }

        return sizes;
    }

    private Map<Integer, Shape> setupShapes() {
        ConfigurationSection configIslandShapes = plugin.getConfig().getConfigurationSection("islandShapes");

        Map<Integer, Shape> shapes = new HashMap<>();

        if (configIslandShapes == null) return shapes;

        for (String key : configIslandShapes.getKeys(false)) {
            String fileName = plugin.getConfig().getString("islandShapes." + key);

            Shape shape = plugin.shapesLoader.loadFromFile(fileName);

            if (shape != null) {
                shapes.put(shape.getWidth(), shape);
                plugin.getLogger().info("Added shape " + shape.file.getName() + " for islandSize " + shape.getWidth() + ".");
            }

        }

        return shapes;
    }

    // TODO:
    //  - Custom island shapes
    //  - Island generation in custom locations outside of the grid. Bigger sizes.
    //  - Generation cooldown
    //  - /ContainerTrust etc.
    //  - Fix giant trees cutting off from top.
}
