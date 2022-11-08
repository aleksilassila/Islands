package me.aleksilassila.islands;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Config {
    private final Islands islands;
    private final Plugin plugin;
    private final FileConfiguration config;

    public final Map<String, Integer> definedIslandSizes;

    public final boolean disableMobs;
    public final boolean voidTeleport;
    public final boolean islandDamage;
    public final boolean restrictFlow;
    public final boolean syncTime;
    public final boolean overrideBedSpawns;
    public final boolean preserveWildernessPositions;
    public final boolean disableWilderness;
    public final int islandSpacing;
    public final int verticalSpacing;

    public final int biomeSearchJumpBlocks;
    public final int biomeSearchSize;
    public final int maxLocationsPerBiome;
    public final List<String> biomeBlacklist;
    public final int biggestIslandSize;

    public final boolean proceduralShapes;
    public final double stalactiteSpacing;
    public final int stalactiteHeight;
    public final double generationDelay;
    public final Map<Material, Material> blockReplacements;

    public Config(Islands islands) {
        this.islands = islands;
        this.plugin = islands.plugin;
        this.config = plugin.getConfig();

        this.definedIslandSizes = parseIslandSizes();

        this.voidTeleport = config.getBoolean("voidTeleport");
        this.restrictFlow = config.getBoolean("restrictIslandBlockFlows");
        this.disableMobs = config.getBoolean("disableMobsOnIslands");
        this.islandDamage = config.getBoolean("islandDamage", false);
        this.syncTime = config.getBoolean("syncTime");
        this.overrideBedSpawns = config.getBoolean("overrideBedSpawns");
        this.preserveWildernessPositions = config.getBoolean("preserveWildernessPositions");
        this.disableWilderness = config.getBoolean("disableWilderness");

        this.islandSpacing = config.getInt("generation.islandGridSpacing");
        this.verticalSpacing = config.getInt("generation.islandGridVerticalSpacing");

        this.biomeSearchJumpBlocks = config.getInt("generation.searchJump");
        this.biomeSearchSize = config.getInt("generation.biomeSearchArea");
        this.maxLocationsPerBiome = config.getInt("generation.maxVariationsPerBiome");
        this.biomeBlacklist = config.getStringList("biomeBlacklist");
        this.biggestIslandSize = config.getInt("generation.minBiomeSize");

        this.generationDelay = plugin.getConfig().getDouble("generation.generationDelayInTicks");
        this.proceduralShapes = plugin.getConfig().getBoolean("useProceduralShapes", true);
        this.stalactiteHeight = plugin.getConfig().getInt("generation.stalactiteLength", 8);
        this.stalactiteSpacing = plugin.getConfig().getDouble("generation.stalactiteSpacing", 2);

        this.blockReplacements = getBlockReplacements();
    }

    private Map<Material, Material> getBlockReplacements() {
        Map<Material, Material> map = new HashMap<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("replaceOnGeneration");

        if (section != null) {
            for (String material : section.getKeys(false)) {
                Material materialToReplace = Material.getMaterial(material.toUpperCase());
                Material newMaterial = plugin.getConfig().getString("replaceOnGeneration." + material) != null
                        ? Material.getMaterial(section.getString(material).toUpperCase())
                        : null;

                if (materialToReplace != null && newMaterial != null) {
                    map.put(materialToReplace, newMaterial);
                    plugin.getLogger().info("Replacing " + materialToReplace.name() + " with " + newMaterial.name());
                } else {
                    if (materialToReplace == null) {
                        plugin.getLogger().warning("Material not found: " + material);
                    }

                    if (newMaterial == null) {
                        plugin.getLogger().warning("Material not found: " + plugin.getConfig().getString("replaceOnGeneration." + material));
                    }
                }
            }
        }

        return map;
    }

    private Map<String, Integer> parseIslandSizes() {
        ConfigurationSection configIslandSizes = config.getConfigurationSection("islandSizes");
        Map<String, Integer> sizes = new HashMap<>();

        if (configIslandSizes == null) {
            plugin.getLogger().severe("PLEASE DEFINE AT LEAST 1 ISLAND SIZE IN config.yml UNDER islandSizes:");
            plugin.getPluginLoader().disablePlugin(plugin);
            return sizes;
        }

        for (String size : configIslandSizes.getKeys(false)) {
            int parsedSize = config.getInt("islandSizes." + size);

            if (parsedSize <= 0) {
                plugin.getLogger().severe("Island size " + size + " has to be an integer and bigger than 0. Ignoring " + size + ".");
                continue;
            }

            sizes.put(size.toUpperCase(), parsedSize);
        }

        return sizes;
    }

    boolean validateConfig() {
        if (config.getDefaults() == null) {
            plugin.getLogger().severe("Error copying defaults to config.");

            return true;
        }

        String out = "";

        for (String defaultKey : config.getDefaults().getKeys(false)) {
            Object defaultValue = config.getDefaults().get(defaultKey);

            if (defaultValue instanceof ConfigurationSection) {
                if (!validateSection(defaultKey)) {
                    return false;
                }
                plugin.getLogger().severe("Config is missing value for " + defaultKey
                        + ". Copied default value to config. Make sure your config is valid, or you might run into errors!");
            } else if (!(defaultValue instanceof List) && !config.getKeys(false).contains(defaultKey)) {
                out = out + "\n" + defaultKey + ": " + defaultValue;
            }
        }

        if (out.length() == 0) return true;

        try { // If using getConfig().set(), all comments will be erased.
            Files.write(Paths.get(plugin.getDataFolder() + "/config.yml"), out.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean validateSection(String defaultKey) {
        List<String> DONT_VALIDATE = new ArrayList<>(Arrays.asList(
                "biomeBlacklist", "excludeShapes", "illegalIslandNames", "replaceOnGeneration", "groupLimits"));
        List<String> SOFT_VALIDATE = new ArrayList<>(Arrays.asList("islandSizes", "islandPrices"));

        ConfigurationSection section = config.getConfigurationSection(defaultKey);
        ConfigurationSection defaultSection = config.getDefaultSection().getConfigurationSection(defaultKey);

        if (section == null || section.getKeys(false).size() == 0) {
            if (DONT_VALIDATE.contains(defaultKey)) {
                plugin.getLogger().warning("Config is missing " + defaultKey);
                return true;
            } else {
                plugin.getLogger().severe("Config is missing section " + defaultKey +
                        ". You can find all the required values in Islands wiki: " +
                        "https://github.com/aleksilassila/Islands/wiki/config.yml. Disabling Islands.");
                return false;
            }
        } else if (DONT_VALIDATE.contains(defaultKey)) {
            return true;
        } else if (SOFT_VALIDATE.contains(defaultKey)) {
            if (section.getKeys(false).size() == 0) {
                plugin.getLogger().severe("Config is missing section " + defaultKey);
                return false;
            } else return true;
        } else {
            for (String key : defaultSection.getKeys(false)) {
                if (!section.getKeys(false).contains(key)) {
                    plugin.getLogger().severe("Config is missing key " + defaultKey + "." + key +
                            ". You can find all the required values in Islands wiki: " +
                            "https://github.com/aleksilassila/Islands/wiki/config.yml. Disabling Islands.");
                    return false;
                }
            }
        }

        return true;
    }
}
