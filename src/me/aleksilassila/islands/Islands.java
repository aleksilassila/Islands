package me.aleksilassila.islands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.generation.IslandGeneration;
import me.aleksilassila.islands.generation.Shape;
import me.aleksilassila.islands.generation.ShapesLoader;
import me.aleksilassila.islands.listeners.Listeners;
import me.aleksilassila.islands.listeners.ProtectionListeners;
import me.aleksilassila.islands.utils.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Islands extends JavaPlugin {
    public World islandsWorld;
    public World islandsSourceWorld;
    public World wildernessWorld;

    private FileConfiguration islandsConfig;
    private File islandsConfigFile;
    private FileConfiguration biomesCache;
    private File biomesCacheFile;

    public Permission perms = null;
    public Economy econ = null;
    public WorldEditPlugin worldEdit = null;
    public ShapesLoader shapesLoader = null;

    public IslandGeneration islandGeneration;
    public IslandLayout layout;

    public Set<Player> playersWithNoFall = new HashSet<>();
    public HashMap<String, ConfirmItem> confirmations;
    public Map<String, Long> teleportCooldowns;
    public Map<Integer, Double> islandPrices;

    public Map<String, Integer> definedIslandSizes;
    public Map<Integer, List<Shape>> definedIslandShapes;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("No Vault or economy plugin found. Economy disabled.");
        }

        if (!setupPermissions()) {
            getLogger().severe("No Vault found. Some permissions disabled.");
        }

        if (!setupWorldedit()) {
            getLogger().severe("No WorldEdit found. Island shapes disabled.");
        }

        new UpdateChecker(this, 84303).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("You are up to date.");
            } else {
                getLogger().info("There's a new update available!");
            }
        });

        if (new File(getDataFolder() + "/config.yml").exists())
            if (!validateConfig()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        else saveDefaultConfig();

        initIslandsConfig();
        initBiomesCache();

        new ConfigMigrator(this);

        islandsWorld = getIslandsWorld();
        islandsSourceWorld = getSourceWorld();
        if (!getConfig().getBoolean("disableWilderness")) {
            wildernessWorld = getWilderness();
        }

        // ISLANDS
        Messages.init(this);

        teleportCooldowns = new HashMap<>();
        confirmations = new HashMap<>();

        definedIslandSizes = setupSizes();
        definedIslandShapes = setupShapes();

        islandGeneration = new IslandGeneration(this);
        layout = new IslandLayout(this);

        new IslandCommands(this);

        new Listeners(this);
        new ProtectionListeners(this);

        int pluginId = 8974;
        new Metrics(this, pluginId);

        getLogger().info("Islands enabled!");
    }

    @Nullable
    public String createNewIsland(Biome biome, int islandSize, Player player) throws IllegalArgumentException {
        Shape shape = definedIslandShapes.getOrDefault(islandSize, null) != null
                ? definedIslandShapes.get(islandSize).get(new Random().nextInt(definedIslandShapes.get(islandSize).size()))
                : null;

        int height = shape != null
                ? shape.getHeight() + islandSize / 2
                : islandSize;

        boolean random = biome == null;

        if (biome == null) {
            biome = islandGeneration.biomes.getRandomBiome(islandSize);
        }

        String islandId = layout.createIsland(player.getUniqueId(), islandSize, height, biome);
        try {
            boolean success = islandGeneration.copyIsland(
                    player,
                    biome,
                    islandSize,
                    new Vector(
                            getIslandsConfig().getInt(islandId + ".x"),
                            getIslandsConfig().getInt(islandId + ".y"),
                            getIslandsConfig().getInt(islandId + ".z")
                    ),
                    false,
                    islandId,
                    shape,
                    random
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

    public boolean recreateIsland(String islandId, Biome biome, int islandSize, Player player) throws IllegalArgumentException {
        Shape shape = definedIslandShapes.getOrDefault(islandSize, null) != null
                ? definedIslandShapes.get(islandSize).get(new Random().nextInt(definedIslandShapes.get(islandSize).size()))
                : null;

        int height = shape != null
                ? shape.getHeight() + islandSize / 2
                : islandSize;

        boolean random = biome == null;

        if (biome == null) {
            biome = islandGeneration.biomes.getRandomBiome(islandSize);
        }

        layout.updateIsland(islandId, islandSize, height, biome);

        try {
            return islandGeneration.copyIsland(
                    player,
                    biome,
                    islandSize,
                    new Vector(
                            getIslandsConfig().getInt(islandId + ".x"),
                            getIslandsConfig().getInt(islandId + ".y"),
                            getIslandsConfig().getInt(islandId + ".z")
                    ),
                    true,
                    islandId,
                    shape,
                    random
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

    @Nullable
    public String parseIslandSize(int size) {
        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedIslandSizes.get(definedSize) == size) return definedSize;
        }

        return null;
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
        for (String definedSize : definedIslandSizes.keySet()) {
            if (definedIslandSizes.get(definedSize) == islandSize) return Permissions.command.create + "." + definedSize;
        }

        return Permissions.command.createCustom;
    }

    World getIslandsWorld() {
        String name = Optional.ofNullable(getConfig().getString("islandsWorldName")).orElse("world");

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equals(name)) {
                getLogger().info("Islands world set to " + name);
                return world;
            }
        }

        getLogger().info("No islands world found. Creating one called " + name + "...");

        World world = new WorldCreator(name).createWorld();
        world.setDifficulty(Difficulty.NORMAL);

        return world;
    }

    World getWilderness() {
        String name = Optional.ofNullable(getConfig().getString("wildernessWorldName")).orElse("wilderness");

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equals(name)) {
                getLogger().info("Wilderness world set to " + name);
                return world;
            }
        }

        getLogger().info("No wilderness found. Creating one called " + name + "...");

        World world = new WorldCreator(name).createWorld();
        world.setDifficulty(Difficulty.HARD);

        return world;
    }

    World getSourceWorld() {
        for (World world : Bukkit.getServer().getWorlds()) {
            if (world.getName().equals("islandsSource")) {
                getLogger().info("Islands source world set to islandsSource");
                return world;
            }
        }

        WorldCreator wc = new WorldCreator("islandsSource");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.NORMAL);
        wc.generateStructures(false);

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.PEACEFUL);

        getLogger().info("Islands source world set to islandsSource");

        return world;
    }

    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("vault") == null) return false;
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            perms = rsp.getProvider();
            return true;
        }
        return false;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        islandPrices = new HashMap<>();

        for (String size : Objects.requireNonNull(getConfig().getConfigurationSection("islandSizes")).getKeys(false)) {
            if (getConfig().getDouble("islandPrices." + size) > 0) {
                islandPrices.put(getConfig().getInt("islandSizes." + size), getConfig().getDouble("islandPrices." + size));
            }
        }

        return true;
    }

    private boolean setupWorldedit() {
        worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (worldEdit != null) {
            shapesLoader = new ShapesLoader(this);
            return true;
        } else return false;
    }

    private Map<String, Integer> setupSizes() {
        ConfigurationSection configIslandSizes = getConfig().getConfigurationSection("islandSizes");
        Map<String, Integer> sizes = new HashMap<>();

        if (configIslandSizes == null) {
            getLogger().severe("PLEASE DEFINE AT LEAST 1 ISLAND SIZE IN config.yml UNDER islandSizes:");
            getPluginLoader().disablePlugin(this);
            return sizes;
        }

        for (String size : configIslandSizes.getKeys(false)) {
            int parsedSize = getConfig().getInt("islandSizes." + size);

            if (parsedSize <= 0) {
                getLogger().severe("Island size " + size + " has to be an integer and bigger than 0. Ignoring " + size + ".");
                continue;
            }

            sizes.put(size.toUpperCase(), parsedSize);
        }

        return sizes;
    }

    private Map<Integer, List<Shape>> setupShapes() {
        if (worldEdit == null) return new HashMap<>();

        return shapesLoader.loadAll();
    }

    public FileConfiguration getIslandsConfig() {
        return this.islandsConfig;
    }

    public void saveIslandsConfig() {
        try {
            islandsConfig.save(islandsConfigFile);
        } catch (IOException e) {
            getLogger().severe("Unable to save islandsConfig");
        }
    }

    private void initIslandsConfig() {
        islandsConfigFile = new File(getDataFolder(), "islands.yml");
        if (!islandsConfigFile.exists()) {
            islandsConfigFile.getParentFile().mkdirs();
            saveResource("islands.yml", false);
         }

        islandsConfig = new YamlConfiguration();
        try {
            islandsConfig.load(islandsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getBiomesCache() {
        return this.biomesCache;
    }

    public void saveBiomesConfig() {
        try {
            biomesCache.save(biomesCacheFile);
        } catch (IOException e) {
            getLogger().severe("Unable to save biomesConfig");
        }
    }

    public void clearBiomesCache() {
        biomesCacheFile.delete();
        initBiomesCache();
    }

    private void initBiomesCache() {
        biomesCacheFile = new File(getDataFolder(), "biomeCache.yml");
        if (!biomesCacheFile.exists()) {
            biomesCacheFile.getParentFile().mkdirs();
            saveResource("biomeCache.yml", false);
         }

        biomesCache = new YamlConfiguration();
        try {
            biomesCache.load(biomesCacheFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private boolean validateConfig() {
        if (getConfig().getDefaults() == null) {
            getLogger().severe("Error copying defaults to config.");

            return true;
        }

        String out = "";

        for (String defaultKey : getConfig().getDefaults().getKeys(false)) {
            Object defaultValue = getConfig().getDefaults().get(defaultKey);

            if (defaultValue instanceof ConfigurationSection) {
                if (!validateSection(defaultKey)) {
                    return false;
                }
            } else if (!(defaultValue instanceof List) && !getConfig().getKeys(false).contains(defaultKey)) {
                getLogger().severe("Config is missing value for " + defaultKey
                        + ". Copied default value to config. Make sure your config is valid, or you might run into errors!");
                out = out + "\n" + defaultKey + ": " + defaultValue;
            }
        }

        if (out.length() == 0) return true;

        try {
            Files.write(Paths.get(getDataFolder() + "/config.yml"), out.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean validateSection(String defaultKey) {
        List<String> DONT_VALIDATE = new ArrayList<>(Arrays.asList(
                "biomeBlacklist", "illegalIslandNames", "replaceOnGeneration", "groupLimits"));
        List<String> SOFT_VALIDATE = new ArrayList<>(Arrays.asList("islandSizes", "islandPrices"));

        ConfigurationSection section = getConfig().getConfigurationSection(defaultKey);
        ConfigurationSection defaultSection = getConfig().getDefaultSection().getConfigurationSection(defaultKey);

        if (section == null || section.getKeys(false).size() == 0) {
            if (DONT_VALIDATE.contains(defaultKey)) {
                getLogger().warning("Config is missing " + defaultKey);
                return true;
            } else {
                getLogger().severe("Config is missing section " + defaultKey + ". Disabling Islands.");
                return false;
            }
        } else if (DONT_VALIDATE.contains(defaultKey)) {
            return true;
        } else if (SOFT_VALIDATE.contains(defaultKey)) {
            if (section.getKeys(false).size() == 0) {
                getLogger().severe("Config is missing section " + defaultKey);
                return false;
            } else return true;
        } else {
            for (String key : defaultSection.getKeys(false)) {
                if (!section.getKeys(false).contains(key)) {
                    getLogger().severe("Config is missing key " + defaultKey + "." + key + ". Disabling islands.");
                    return false;
                }
            }
        }

        return true;
    }

    // TODO:
    //  - Island generation in custom locations outside of the grid. Bigger sizes.
    //  - Generation cooldown
    //  - /ContainerTrust etc. Maybe transition to protection plugin?
    //  - Fix giant trees cutting off from top.
    //  - API ??
    //  - Transition into using a database
}
