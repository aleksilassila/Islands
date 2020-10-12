package me.aleksilassila.islands;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.commands.GUIs.VisitGui;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.utils.ConfirmItem;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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

    public enum IslandSize {
        SMALL(32), // 32*32
        NORMAL(64), // 64*64
        BIG(80); // 80*80

        private final int size;

        public int getSize() {
            return size;
        }

        IslandSize(int size) {
            this.size = size;
        }
    }

    public Islands(World world, World sourceWorld, Main plugin) {
        this.plugin = plugin;
        this.sourceWorld = sourceWorld;
        this.teleportCooldowns = new HashMap<>();
        this.confirmations = new HashMap<>();

        this.islandGeneration = new IslandGeneration(this);
        this.layout = new IslandLayout(this);

        this.visitGui = new VisitGui(plugin);
    }

    public static class IslandsException extends java.lang.Exception {
        public IslandsException(String message) {
            super(message);
        }
    }

    @NotNull
    public int parseIslandSize(IslandSize size) {
        switch (size) {
            case SMALL:
                return plugin.getConfig().getInt("island.SMALL");
            case BIG:
                return plugin.getConfig().getInt("island.BIG");
            case NORMAL:
            default:
                return plugin.getConfig().getInt("island.NORMAL");

        }
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
                    0
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
            boolean success = islandGeneration.copyIsland(
                    player,
                    biome,
                    islandSize,
                    plugin.getIslandsConfig().getInt(islandId + ".x"),
                    plugin.getIslandsConfig().getInt(islandId + ".y"),
                    plugin.getIslandsConfig().getInt(islandId + ".z"),
                    shouldClearArea,
                    plugin.getIslandsConfig().getInt(islandId + ".xIndex"),
                    plugin.getIslandsConfig().getInt(islandId + ".zIndex")
            );

            return success;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }
}
