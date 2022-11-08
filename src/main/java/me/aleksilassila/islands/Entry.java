package me.aleksilassila.islands;

import me.aleksilassila.islands.plugins.GriefPrevention;
import me.aleksilassila.islands.utils.Permissions;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Entry {
    private final IslandsConfig islandsConfig;
    private final FileConfiguration config;

    private final GriefPrevention griefPrevention;

    private final World islandsWorld;


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

    public boolean shouldUpdate = false;

    public Entry(IslandsConfig islandsConfig, String islandId) {
        this.islandsConfig = islandsConfig;
        this.config = islandsConfig.getConfig();

        this.griefPrevention = islandsConfig.islands.griefPrevention;

        this.islandsWorld = islandsConfig.islands.getIslandsWorld();

        this.islandId = islandId;

        this.xIndex = config.getInt(islandId + ".xIndex");
        this.zIndex = config.getInt(islandId + ".zIndex");

        try {
            this.uuid = UUID.fromString(config.getString(islandId + ".UUID"));
        } catch (Exception ignored) {
        }
        this.homeId = config.getInt(islandId + ".home");
        this.name = config.getString(islandId + ".name", String.valueOf(homeId));
        this.size = config.getInt(islandId + ".size");
        this.height = config.getInt(islandId + ".height");
        this.isPublic = config.getBoolean(islandId + ".public", false);
        this.biome = Biome.valueOf(config.getString(islandId + ".biome", "PLAINS"));
        this.homeId = config.getInt(islandId + ".home", -1);
        this.spawnPosition = new int[]{
                config.getInt(islandId + ".spawnPoint.x", 0),
                config.getInt(islandId + ".spawnPoint.z", 0)
        };
        this.y = config.getInt(islandId + ".y");
        this.isSpawn = config.getBoolean(islandId + ".isSpawn", false);

        this.claimId = config.getLong(islandId + ".claimId", -1);

        if (this.claimId == -1 && griefPrevention.isEnabled()) {
            deleteClaims();
            this.claimId = createClaims(xIndex, zIndex, size, uuid);
            this.shouldUpdate = true;
        }
    }

    public Entry(IslandsConfig islandsConfig, int xIndex, int zIndex, int size, int height, UUID uuid, String name, Biome biome) {
        this.islandsConfig = islandsConfig;
        this.config = islandsConfig.getConfig();

        this.griefPrevention = islandsConfig.islands.griefPrevention;

        this.islandsWorld = islandsConfig.islands.getIslandsWorld();

        this.islandId = islandsConfig.posToIslandId(xIndex, zIndex);
        this.xIndex = xIndex;
        this.zIndex = zIndex;
        this.size = size;
        this.height = height;
        this.uuid = uuid;
        this.name = name;
        this.biome = biome;
        this.homeId = islandsConfig.getNewHomeId(uuid);

        this.claimId = createClaims(xIndex, zIndex, size, uuid);

        int[][] ic = islandsConfig.getIslandCorner(xIndex, zIndex, size);
        this.spawnPosition = new int[]{
                ic[0][0] + size / 2,
                ic[0][1] + size / 2
        };

        this.y = islandsConfig.getIslandY(xIndex, zIndex);

        this.isPublic = false;
        this.isSpawn = false;

    }

    public void delete() {
        islandsConfig.getConfig().set(islandId, null);
        deleteClaims();
        islandsConfig.entries.remove(islandId);

        islandsConfig.saveIslandsConfig();
    }

    public void writeToConfig() {
        int[][] ic = islandsConfig.getIslandCorner(xIndex, zIndex, size);

        String islandId = islandsConfig.posToIslandId(xIndex, zIndex);

        config.set(islandId + ".xIndex", xIndex);
        config.set(islandId + ".zIndex", zIndex);

        config.set(islandId + ".x", ic[0][0]);
        config.set(islandId + ".y", y);
        config.set(islandId + ".z", ic[0][1]);

        config.set(islandId + ".spawnPoint.x", spawnPosition[0]);
        config.set(islandId + ".spawnPoint.z", spawnPosition[1]);

        config.set(islandId + ".UUID", uuid == null ? "Server" : uuid.toString());
        config.set(islandId + ".name", name);
        config.set(islandId + ".home", homeId);
        config.set(islandId + ".size", size);
        config.set(islandId + ".height", height);
        config.set(islandId + ".public", isPublic);
        config.set(islandId + ".biome", biome.name());

        config.set(islandId + ".claimId", claimId);
        config.set(islandId + ".isSpawn", isSpawn);
    }

    public void setSpawnPosition(int x, int z) {
        spawnPosition = new int[]{x, z};
        shouldUpdate = true;
    }

    @NotNull
    public Location getIslandSpawn() {
        int highest = islandsWorld.getHighestBlockAt(spawnPosition[0], spawnPosition[1]).getY();
        return new Location(
                islandsWorld,
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
        this.homeId = islandsConfig.getNewHomeId(player.getUniqueId());
        deleteClaims();
        this.claimId = createClaims(xIndex, zIndex, size, player.getUniqueId());

        shouldUpdate = true;
    }

    public void giveToServer() {
        this.uuid = null;
        this.homeId = -1;
        deleteClaims();
        this.claimId = createClaims(xIndex, zIndex, size, null);

        shouldUpdate = true;
    }

    public void setSpawnIsland() {
        isSpawn = !isSpawn;
        islandsConfig.spawnIsland = isSpawn ? this : null;

        shouldUpdate = true;
    }

    public void resizeClaim(int islandSize) {
        if (!griefPrevention.isEnabled()) return;
        int[][] ic = islandsConfig.getIslandCorner(xIndex, zIndex, islandSize);

        Claim c = griefPrevention.griefPrevention.dataStore.getClaimAt(new Location(
                islandsWorld,
                spawnPosition[0],
                50,
                spawnPosition[1]), true, false, null);
        griefPrevention.griefPrevention.dataStore.resizeClaim(c,
                ic[0][0], ic[1][0],
                0, 255,
                ic[0][1], ic[1][1], Bukkit.getPlayer(uuid));
    }

    private long createClaims(int xIndex, int zIndex, int size, UUID uuid) {
        if (!griefPrevention.isEnabled()) return -1;
        int[][] ipc = islandsConfig.getIslandPlotCorner(xIndex, zIndex);
        CreateClaimResult r = griefPrevention.griefPrevention.dataStore.createClaim(islandsWorld,
                ipc[0][0], ipc[1][0],
                0, 255,
                ipc[0][1], ipc[1][1],
                null, null, null, null);

        if (r.succeeded) {
            long claimId = r.claim.getID();
            int[][] ic = islandsConfig.getIslandCorner(xIndex, zIndex, size);


            Claim subClaim = griefPrevention.griefPrevention.dataStore.createClaim(islandsWorld,
                    ic[0][0], ic[1][0],
                    0, 255,
                    ic[0][1], ic[1][1],
                    null, r.claim, null, null).claim;
            if (uuid != null) {
                subClaim.setPermission(uuid.toString(), ClaimPermission.Build);
                addClaimManager(subClaim, uuid.toString());

                Player p = Bukkit.getOfflinePlayer(uuid).getPlayer();

                if (Plugin.instance.getConfig().getBoolean("GPAccessWholePlot") ||
                        (p != null && p.hasPermission(Permissions.bypass.interactInPlot))) {
                    r.claim.setPermission(uuid.toString(), ClaimPermission.Build);
                    addClaimManager(r.claim, uuid.toString());
                }
            }

            return claimId;
        } else {
            Plugin.instance.getLogger().severe("Error creating claim for island at plot " + xIndex + ", " + zIndex);
            return -1;
        }
    }

    private void addClaimManager(Claim claim, String uuid) {
        if (!claim.managers.contains(uuid)) {
            claim.managers.add(uuid);
            griefPrevention.griefPrevention.dataStore.saveClaim(claim);
        }
    }

    private void deleteClaims() {
        if (!griefPrevention.isEnabled()) return;
        DataStore dataStore = griefPrevention.griefPrevention.dataStore;
        Claim c = dataStore.getClaim(this.claimId);

        if (c == null) {
            int[][] ic = islandsConfig.getIslandCorner(xIndex, zIndex, size);
            c = dataStore.getClaimAt(new Location(islandsWorld, ic[0][0], 50, ic[0][1]), true, true, null);
        }

        if (c != null) {
            dataStore.deleteClaim(c);
        }
        this.claimId = -1;
    }

    public void teleport(Player player) {
        if (islandsConfig.configuration.islandDamage)
            islandsConfig.islands.playersWithNoFall.add(player);
        player.teleport(getIslandSpawn());
    }
}
