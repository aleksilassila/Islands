package me.aleksilassila.islands.generation;

import me.aleksilassila.islands.Islands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IslandGrid {
    private Islands islands;

    private int islandsInARow;
    private int islandSpacing;

    public IslandGrid(Islands instance) {
        this.islands = instance;

        this.islandsInARow = instance.plugin.getConfig().getInt("generation.islandsInARow");
        this.islandSpacing = instance.plugin.getConfig().getInt("generation.islandSpacing");

        Bukkit.broadcastMessage("INT IS " + islandSpacing);
    }

    public static class IslandGridException extends java.lang.Exception {
        public IslandGridException(String message) {
            super(message);
        }
    }

    private FileConfiguration getConfig() {
        return islands.plugin.getIslandsConfig();
    }

    public String getFirstIslandId(UUID uuid) throws IslandGridException {
        ConfigurationSection section = getConfig().getConfigurationSection("islands");

        if (section == null) {
            throw new IslandGridException("No islands found.");
        }

        Set<String> islands = section.getKeys(false);

        for (String islandId : islands) {
            if (getConfig().getString("islands." + islandId + ".UUID").equals(uuid.toString())) {
                return islandId;
            }
        }

        throw new IslandGridException("Island not found");
    }

    public List<String> getAllIslandIds(UUID uuid) throws IslandGridException {
        List<String> islands = new ArrayList<>();

        ConfigurationSection section = getConfig().getConfigurationSection("islands");

        if (section == null) {
            throw new IslandGridException("No islands found.");
        }

        Set<String> allIslands = section.getKeys(false);


        for (String islandId : allIslands) {
            if (getConfig().getString("islands." + islandId + ".UUID").equals(uuid.toString())) {
                islands.add(islandId);
            }
        }

        return islands;
    }

    public String getIslandId(UUID uuid, String name) throws IslandGridException {
        List<String> allIslands = getAllIslandIds(uuid);

        if (allIslands == null) {
            throw new IslandGridException("No islands found.");
        }

        for (String islandId : allIslands) {
            if (getConfig().getString("islands." + islandId + ".name").equals(name)) {
                return islandId;
            }
        }

        throw new IslandGridException("No island matched given name,");
    }

    public Location getIslandSpawn(String islandId) throws IslandGridException {
        if (getConfig().getConfigurationSection("islands."+islandId) != null) {
            return new Location(
                    islands.plugin.islandsWorld,
                    getConfig().getInt("islands." + islandId + ".spawnPoint.x"),
                    getConfig().getInt("islands." + islandId + ".y") + 100,
                    getConfig().getInt("islands." + islandId + ".spawnPoint.z")
            );
        } else {
            throw new IslandGridException("Island not found");
        }
    }

    private int getIslandY(int xIndex, int zIndex) {
        int islandIndex = (xIndex * islandsInARow + zIndex);
        return 20 + ((islandIndex + xIndex) % 3) * 70;
    }

    private String addIslandToConfig(int xIndex, int zIndex, int islandSize, UUID uuid, String name){
        int realX = xIndex * islandSpacing;
        int realY = getIslandY(xIndex, zIndex);
        int realZ = zIndex * islandSpacing;

        String islandId = String.valueOf((int) Math.floor(Math.random() * 10000000));

        getConfig().set("islands."+islandId+".xIndex", xIndex);
        getConfig().set("islands."+islandId+".zIndex", zIndex);

        getConfig().set("islands."+islandId+".x", realX);
        getConfig().set("islands."+islandId+".y", realY);
        getConfig().set("islands."+islandId+".z", realZ);

        getConfig().set("islands."+islandId+".spawnPoint.x", realX + islandSize / 2);
        getConfig().set("islands."+islandId+".spawnPoint.z", realZ + islandSize / 2);

        getConfig().set("islands."+islandId+".UUID", uuid.toString());
        getConfig().set("islands."+islandId+".name", name);
        getConfig().set("islands."+islandId+".size", islandSize);

        islands.plugin.saveIslandsConfig();

        return islandId;
    }

    public String createIsland(UUID uuid, int islandSize) throws IslandGridException {
        ConfigurationSection section = getConfig().getConfigurationSection("islands");

        if (section == null) {
            return addIslandToConfig(0, 1, islandSize, uuid, "1");
        }

        Set<String> islands = section.getKeys(false);

        for (int x = 0; x > -1; x++) {
            loop:
            for (int z = 1; z <= 3; z++) {
                for (String islandId : islands) {
                    if (getConfig().getInt("islands." + islandId + ".xIndex") == x && getConfig().getInt("islands." + islandId + ".zIndex") == z) {
                        continue loop;
                    }
                }

                int numberOfPreviousIslands;

                try {
                    numberOfPreviousIslands = getAllIslandIds(uuid).size();
                } catch (IslandGridException e) {
                    numberOfPreviousIslands = 0;
                }

                return addIslandToConfig(x, z, islandSize, uuid, String.valueOf(numberOfPreviousIslands + 1));
            }
        }


        // Nothing fancy so far
//        for (int layer = 1; layer == -1; layer++) {
//            for (int index = 0; index == -1; index++) {
//                int nOfPositions = (layer - 1) * 2 + 1;
//            }
//        }

        throw new IslandGridException("Error creating island");
    }

    public boolean nameIsland(String name, UUID uuid) {
        if (getConfig().getConfigurationSection("islands." + name) != null) {
            return false;
        }
        try {
            getConfig().set("islands."+ getFirstIslandId(uuid)+".name", name);
            islands.plugin.saveIslandsConfig();

        } catch (IslandGridException e) {
            return false;
        }

        return true;
    }

    public boolean isBlockInIsland(int x, int y, int z) {
        int xIndex = x / islandSpacing;
        int zIndex = z / islandSpacing;
        int islandLowY = getIslandY(xIndex, zIndex);

        int relativeX = x - xIndex * islandSpacing;
        int relativeZ = z - zIndex * islandSpacing;
        int relativeY = y - islandLowY;

        int islandSize = 64; // CHANGE THIS

        return islands.islandGeneration.isBlockInShape(relativeX, relativeY, relativeZ, islandSize);
    }

    // deleteIsland
    // moveIsland
}
