package me.aleksilassila.islands;

import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.utils.BiomeMaterials;
import me.aleksilassila.islands.utils.Permissions;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public enum IslandsConfig {
    INSTANCE;

    public final int islandSpacing;
    public final int verticalSpacing;
    public final boolean islandDamage;

    public static HashMap<String, Entry> entries;
    public static Entry spawnIsland = null;

    private static FileConfiguration config;
    private static File configFile;

    IslandsConfig() {
        this.islandSpacing = Islands.instance.getConfig().getInt("generation.islandGridSpacing");
        this.verticalSpacing = Islands.instance.getConfig().getInt("generation.islandGridVerticalSpacing");
        this.islandDamage = Islands.instance.getConfig().getBoolean("islandDamage", false);
    }

    public static FileConfiguration getConfig() {
        if (config != null) return config;

        configFile = new File(Islands.instance.getDataFolder(), "islands.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            Islands.instance.saveResource("islands.yml", false);
         }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        entries = loadEntries();

        return config;
    }

    public static void saveIslandsConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Islands.instance.getLogger().severe("Unable to save islandsConfig");
        }
    }

    public static HashMap<String, Entry> loadEntries() {
        HashMap<String, Entry> entries = new HashMap<>();
        for (String islandId : getConfig().getKeys(false)) {
            Entry e = new Entry(islandId);
            entries.put(islandId, e);
            if (e.isSpawn) spawnIsland = e;
        }

        return entries;
    }

    public static void updateEntries() {
        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            if (e.shouldUpdate) {
                e.writeToConfig();
                e.shouldUpdate = false;
            }
        }

        saveIslandsConfig();
    }

    @Nullable
    public static Entry getEntry(int x, int z, boolean useRawCoordinates) {
        if (!useRawCoordinates) return getEntry(x, z);

        int xIndex = x / INSTANCE.islandSpacing;
        int zIndex = z / INSTANCE.islandSpacing;

        return getEntry(xIndex, zIndex);
    }

    @Nullable
    public static Entry getEntry(int xIndex, int zIndex) { // fixme not finding some islands by raw coordinates
        for (Entry e : entries.values()) {
            if (e.xIndex == xIndex && e.zIndex == zIndex) return e;
        }

        return null;
    }

    public static Entry createIsland(UUID uuid, int islandSize, int height, Biome biome) {
        int index = 0;

        Set<String> islands = entries.keySet();

        while (true) {
            int[] pos = placement.getIslandPos(index);

            if (!islands.contains(posToIslandId(pos[0], pos[1]))) {
                return addIsland(pos[0], pos[1], islandSize, height, uuid, String.valueOf(getNewHomeId(uuid)), biome);
            }

            index++;
        }
    }

    @NotNull
    private static Entry addIsland(int xIndex, int zIndex, int islandSize, int height, UUID uuid, String name, Biome biome) {
        String islandId = posToIslandId(xIndex, zIndex);
        Entry e = new Entry(xIndex, zIndex, islandSize, height, uuid, name, biome);
        entries.put(islandId, e);
        e.writeToConfig();
        saveIslandsConfig();
        return e;
    }

    @NotNull
    public static List<Entry> getOwnedIslands(UUID uuid) {
        List<Entry> islands = new ArrayList<>();

        for (Entry e : entries.values()) {
            if (uuid.equals(e.uuid)) islands.add(e);
        }

        return islands;
    }

    @NotNull
    public static Map<String, Map<String, String>> getIslandsInfo(boolean publicOnly) {
        Map<String, Map<String, String>> islands = new HashMap<>();

        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);

            if (!publicOnly || e.isPublic) {
                String name = e.isPublic ? e.name : islandId;

                Map<String, String> values = new HashMap<>();
                values.put("name", name);
                values.put("owner", e.uuid != null ? e.uuid.toString() : "Server");

                try {
                    String biome = e.biome.toString();
                    values.put("material", BiomeMaterials.valueOf(biome).name());
                } catch (Exception exception) {
                    values.put("material", BiomeMaterials.DEFAULT.name());
                }

                values.put("public", String.valueOf(e.isPublic ? 1 : 0));

                islands.put(islandId, values);
            }
        }

        return islands;
    }

    @NotNull
    public static Map<String, Map<String, String>> getIslandsInfo(UUID uuid) {
        Map<String, Map<String, String>> islands = getIslandsInfo(false);
        Map<String, Map<String, String>> finalIslands = new HashMap<>();

        for (String islandId : entries.keySet()) {
            Entry e = entries.get(islandId);
            if (islands.containsKey(islandId) && uuid.equals(e.uuid))
                finalIslands.put(islandId, islands.get(islandId));
        }

        return finalIslands;
    }

    @NotNull
    public static Map<UUID, Integer> getIslandOwners() {
        Map<UUID, Integer> players = new HashMap<>();

        for (Entry e : entries.values()) {
            if (e.uuid != null) {
                if (players.containsKey(e.uuid)) {
                    players.put(e.uuid, players.get(e.uuid) + 1);
                } else {
                    players.put(e.uuid, 1);
                }
            }
        }

        return players;
    }

    @Nullable
    public static Entry getIslandByName(String name) {
        for (Entry e : entries.values()) {
            if (name.equalsIgnoreCase(e.name) && e.isPublic) {
                return e;
            }
        }

        return null;
    }

    @Nullable
    public static Entry getHomeIsland(UUID uuid, int homeId) {
        List<Entry> allIslands = getOwnedIslands(uuid);

        for (Entry e : allIslands) {
            if (e.homeId == homeId) {
                return e;
            }
        }

        return null;
    }

    @Nullable
    public static int getLowestHome(UUID uuid) {
        List<Entry> allIslands = getOwnedIslands(uuid);

        int lowestHome = -1;

        for (Entry e : allIslands) {
            if (e.homeId != -1 && (e.homeId < lowestHome || lowestHome == -1)) {
                lowestHome = e.homeId;
            }
        }

        return lowestHome;
    }

    /**
     * Checks if block (relative to the position of
     * the island) is inside water flow are.
     *
     * From bottom to up the area is first a half sphere with diameter
     * of the island width and then a cylinder with same diameter.
     */
    public static boolean isBlockInWaterFlowArea(int x, int y, int z) {
        int xIndex = x / INSTANCE.islandSpacing;
        int zIndex = z / INSTANCE.islandSpacing;

        Entry e = getEntry(xIndex, zIndex);
        if (e == null)
            return false;

        int[][] ic = getIslandCorner(xIndex, zIndex, e.size);

        int relativeX = x - ic[0][0];
        int relativeZ = z - ic[0][1];
        int relativeY = y - getIslandY(xIndex, zIndex);

        if (relativeY <= e.size / 2d) {
            return IslandGeneration.isBlockInIslandSphere(relativeX, relativeY, relativeZ, e.size);
        } else {
            return IslandGeneration.isBlockInIslandCylinder(relativeX, relativeZ, e.size);
        }
    }

    public static int getNewHomeId(UUID uuid) {
        List<Integer> homeIds = new ArrayList<>();

        for (Entry e : getOwnedIslands(uuid)) {
            homeIds.add(e.homeId);
        }

        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            if (!homeIds.contains(i)) return i;
        }

        return 0;
    }

    // UTILS

    private static int getIslandY(int xIndex, int zIndex) {
        return 10 + ((xIndex + zIndex) % 3) * INSTANCE.verticalSpacing;
    }

    static String posToIslandId(int xIndex, int zIndex) {
        return xIndex + "x" + zIndex;
    }

    public static int[][] getIslandCorner(int xIndex, int zIndex, int size) {
        return new int[][] {
                new int[] {
                        xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - size / 2,
                        zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 - size / 2
                }, new int[] {
                        xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 + size / 2 - 1,
                        zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing / 2 + size / 2 - 1
                }
        };
    }

    public static int[][] getIslandPlotCorner(int xIndex, int zIndex) {
        return new int[][] {
                new int[] {
                        xIndex * INSTANCE.islandSpacing,
                        zIndex * INSTANCE.islandSpacing
                }, new int[] {
                        xIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing - 1,
                        zIndex * INSTANCE.islandSpacing + INSTANCE.islandSpacing - 1
                }
        };
    }

    public static class Entry {
        public String islandId;
        public int xIndex;
        public int zIndex;
        public int size;
        public int height;
        public long claimId;
        public Biome biome;
        public UUID uuid;
        public String name;
        public int homeId;
        public boolean isPublic;
        public int y;
        public int[] spawnPosition;
        public boolean isSpawn;

        boolean shouldUpdate = false;

        public Entry(String islandId) {
            FileConfiguration fc = getConfig();
            this.islandId = islandId;

            this.xIndex = fc.getInt(islandId + ".xIndex");
            this.zIndex = fc.getInt(islandId + ".zIndex");

            try {
                this.uuid = UUID.fromString(fc.getString(islandId + ".UUID"));
            } catch (Exception ignored) {}
            this.homeId = fc.getInt(islandId + ".home");
            this.name = fc.getString(islandId + ".name", String.valueOf(homeId));
            this.size = fc.getInt(islandId + ".size");
            this.height = fc.getInt(islandId + ".height");
            this.isPublic = fc.getBoolean(islandId + ".public", false);
            this.biome = Biome.valueOf(fc.getString(islandId + ".biome", "PLAINS"));
            this.homeId = fc.getInt(islandId + ".home", -1);
            this.spawnPosition = new int[] {
                    fc.getInt(islandId + ".spawnPoint.x", 0),
                    fc.getInt(islandId + ".spawnPoint.z", 0)
            };
            this.y = fc.getInt(islandId + ".y");
            this.isSpawn = fc.getBoolean(islandId + ".isSpawn", false);

            this.claimId = fc.getLong(islandId + ".claimId", -1);

            if (this.claimId == -1 && GPWrapper.enabled) {
                deleteClaims();
                this.claimId = createClaims(xIndex, zIndex, size, uuid);
                this.shouldUpdate = true;
            }
        }

        public Entry(int xIndex, int zIndex, int size, int height, UUID uuid, String name, Biome biome) {
            this.islandId = posToIslandId(xIndex, zIndex);
            this.xIndex = xIndex;
            this.zIndex = zIndex;
            this.size = size;
            this.height = height;
            this.uuid = uuid;
            this.name = name;
            this.biome = biome;
            this.homeId = getNewHomeId(uuid);

            this.claimId = GPWrapper.enabled ? createClaims(xIndex, zIndex, size, uuid) : -1;

            int[][] ic = getIslandCorner(xIndex, zIndex, size);
            this.spawnPosition = new int[] {
                    ic[0][0] + size / 2,
                    ic[0][1] + size / 2
            };

            this.y = getIslandY(xIndex, zIndex);

            this.isPublic = false;
            this.isSpawn = false;

        }

        public void delete() {
            getConfig().set(islandId, null);
            if (GPWrapper.enabled)
                deleteClaims();
            entries.remove(islandId);

            saveIslandsConfig();
        }

        public void writeToConfig() {
            int[][] ic = getIslandCorner(xIndex, zIndex, size);

            String islandId = posToIslandId(xIndex, zIndex);

            getConfig().set(islandId + ".xIndex", xIndex);
            getConfig().set(islandId + ".zIndex", zIndex);

            getConfig().set(islandId + ".x", ic[0][0]);
            getConfig().set(islandId + ".y", y);
            getConfig().set(islandId + ".z", ic[0][1]);

            getConfig().set(islandId + ".spawnPoint.x", spawnPosition[0]);
            getConfig().set(islandId + ".spawnPoint.z", spawnPosition[1]);

            getConfig().set(islandId + ".UUID", uuid == null ? "Server" : uuid.toString());
            getConfig().set(islandId + ".name", name);
            getConfig().set(islandId + ".home", homeId);
            getConfig().set(islandId + ".size", size);
            getConfig().set(islandId + ".height", height);
            getConfig().set(islandId + ".public", isPublic);
            getConfig().set(islandId + ".biome", biome.name());

            getConfig().set(islandId + ".claimId", claimId);
            getConfig().set(islandId + ".isSpawn", isSpawn);
        }

        public void setSpawnPosition(int x, int z) {
            spawnPosition = new int[] {x, z};
            shouldUpdate = true;
        }

        @NotNull
        public Location getIslandSpawn() {
            int highest = Islands.islandsWorld.getHighestBlockAt(spawnPosition[0], spawnPosition[1]).getY();
            return new Location(
                    Islands.islandsWorld,
                    spawnPosition[0],
                    highest + 80,
                    spawnPosition[1]
            );
        }

        public void unnameIsland() {
            name = String.valueOf(homeId);
            isPublic = false;

            shouldUpdate = true;
        }

        public void nameIsland(String name) {
            this.isPublic = true;
            this.name = name;

            shouldUpdate = true;
        }

        public void giveIsland(OfflinePlayer player) {
            this.uuid = player.getUniqueId();
            this.homeId = getNewHomeId(player.getUniqueId());
            if (GPWrapper.enabled) {
                deleteClaims();
                this.claimId = createClaims(xIndex, zIndex, size, player.getUniqueId());
            }

            shouldUpdate = true;
        }

        public void giveToServer() {
            this.uuid = null;
            this.homeId = -1;
            if (GPWrapper.enabled) {
                deleteClaims();
                this.claimId = createClaims(xIndex, zIndex, size, null);
            }

            shouldUpdate = true;
        }

        public void setSpawnIsland() {
            isSpawn = !isSpawn;
            IslandsConfig.spawnIsland = isSpawn ? this : null;

            shouldUpdate = true;
        }

        public void resizeClaim(int islandSize) {
            int[][] ic = getIslandCorner(xIndex, zIndex, islandSize);

            Claim c = GPWrapper.gp.dataStore.getClaimAt(new Location(
                    Islands.islandsWorld,
                    spawnPosition[0],
                    50,
                    spawnPosition[1]), true, false, null);
            GPWrapper.gp.dataStore.resizeClaim(c,
                    ic[0][0], ic[1][0],
                    0, 255,
                    ic[0][1], ic[1][1], Bukkit.getPlayer(uuid));
        }

        private static long createClaims(int xIndex, int zIndex, int size, UUID uuid) {
            int[][] ipc = getIslandPlotCorner(xIndex, zIndex);
            CreateClaimResult r = GPWrapper.gp.dataStore.createClaim(Islands.islandsWorld,
                ipc[0][0], ipc[1][0],
                0, 255,
                ipc[0][1], ipc[1][1],
                null, null, null, null);

            if (r.succeeded) {
                long claimId = r.claim.getID();
                int[][] ic = getIslandCorner(xIndex, zIndex, size);


                Claim subClaim = GPWrapper.gp.dataStore.createClaim(Islands.islandsWorld,
                ic[0][0], ic[1][0],
                0, 255,
                ic[0][1], ic[1][1],
                null, r.claim, null, null).claim;
                if (uuid != null) {
                    subClaim.setPermission(uuid.toString(), ClaimPermission.Build);
                    addClaimManager(subClaim, uuid.toString());

                    Player p = Bukkit.getOfflinePlayer(uuid).getPlayer();

                    if (Islands.instance.getConfig().getBoolean("GPAccessWholePlot") ||
                            (p != null && p.hasPermission(Permissions.bypass.interactInPlot))) {
                        r.claim.setPermission(uuid.toString(), ClaimPermission.Build);
                        addClaimManager(r.claim, uuid.toString());
                    }
                }

                return claimId;
            } else {
                Islands.instance.getLogger().severe("Error creating claim for island at plot " + xIndex + ", " + zIndex);
                return -1;
            }
        }

        private static void addClaimManager(Claim claim, String uuid) {
            if (!claim.managers.contains(uuid)) {
                claim.managers.add(uuid);
                GPWrapper.gp.dataStore.saveClaim(claim);
            }
        }

        private void deleteClaims() {
            Claim c = GPWrapper.gp.dataStore.getClaim(this.claimId);

            if (c == null) {
                int[][] ic = getIslandCorner(xIndex, zIndex, size);
                c = GPWrapper.gp.dataStore.getClaimAt(new Location(Islands.islandsWorld, ic[0][0], 50, ic[0][1]), true, true, null);
            }

            if (c != null) {
                GPWrapper.gp.dataStore.deleteClaim(c);
            }
            this.claimId = -1;
        }

        public void teleport(Player player) {
            if (INSTANCE.islandDamage)
                Islands.instance.playersWithNoFall.add(player);
            player.teleport(getIslandSpawn());
        }
    }

    public static class placement {
        public static int getLayer(int index) {
            return (int) Math.floor(Math.sqrt(index));
        }

        public static int getLayerSize(int layer) {
            return 2 * layer + 1;
        }

        public static int firstOfLayer(int layer) {
            return layer * layer;
        }

        public static int[] getIslandPos(int index) {
            int layer = getLayer(index);

            int x = Math.min(index - firstOfLayer(layer), layer);
            int z = (index - firstOfLayer(layer) < layer + 1) ? layer : firstOfLayer(layer) + getLayerSize(layer) - 1 - index;

            return new int[]{x, z};
        }

        // TODO: Optimize
        public static int getIslandIndex(int[] pos) {
            int index = 0;
            while (!Arrays.equals(getIslandPos(index), pos)) {
                index++;
            }

            return index;
        }

        public static int getIslandIndex(String islandId) {
            try {
                return getIslandIndex(new int[]{Integer.parseInt(islandId.split("x")[0]), Integer.parseInt(islandId.split("x")[1])});
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }
}
