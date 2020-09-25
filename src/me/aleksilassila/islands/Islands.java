package me.aleksilassila.islands;

import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.generation.IslandGrid;
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
    public IslandGrid grid;

    public Set<Player> playersWithNoFall = new HashSet<>();
    public final HashMap<String, ConfirmItem> confirmations;
    public Map<String, Long> teleportCooldowns;

    public enum IslandSize {
        SMALL, // 32*32
        NORMAL, // 64*64
        BIG, // 80*80
        HUGE
    }

    public Islands(World world, World sourceWorld, Main plugin) {
        this.plugin = plugin;
        this.sourceWorld = sourceWorld;
        this.teleportCooldowns = new HashMap<>();
        this.confirmations = new HashMap<>();

        this.islandGeneration = new IslandGeneration(this);
        this.grid = new IslandGrid(this);

    }

    public static class IslandsException extends java.lang.Exception {
        public IslandsException(String message) {
            super(message);
        }
    }

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
    public String createNewIsland(Biome biome, IslandSize size, Player player) throws IllegalArgumentException {
        int islandSize = parseIslandSize(size);

        String islandId = grid.createIsland(player.getUniqueId(), islandSize);
        try {
            boolean success = islandGeneration.copyIsland(
                    player,
                    biome,
                    islandSize,
                    plugin.getIslandsConfig().getInt("islands." + islandId + ".x"),
                    plugin.getIslandsConfig().getInt("islands." + islandId + ".y"),
                    plugin.getIslandsConfig().getInt("islands." + islandId + ".z")
            );

            if (!success) {
                grid.deleteIsland(islandId);
                return null;
            }

            return islandId;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }

    }

    public boolean regenerateIsland(String islandId, Biome biome, IslandSize islandSize, Player player) throws IllegalArgumentException {
        grid.updateIslandSize(islandId, parseIslandSize(islandSize));

        try {
            boolean success = islandGeneration.copyIsland(
                    player,
                    biome,
                    parseIslandSize(islandSize),
                    plugin.getIslandsConfig().getInt("islands." + islandId + ".x"),
                    plugin.getIslandsConfig().getInt("islands." + islandId + ".y"),
                    plugin.getIslandsConfig().getInt("islands." + islandId + ".z")
            );

            return success;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }
}
