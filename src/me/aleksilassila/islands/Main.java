package me.aleksilassila.islands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.commands.TrustCommands;
import me.aleksilassila.islands.generation.ShapesLoader;
import me.aleksilassila.islands.listeners.IslandsListener;
import me.aleksilassila.islands.utils.UpdateChecker;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Main extends JavaPlugin {

    public World islandsWorld;
    public World islandsSourceWorld;
    public World wildernessWorld;

    private FileConfiguration islandsConfig;
    private File islandsConfigFile;
    private FileConfiguration biomesConfig;
    private File biomesConfigFile;

    public Islands islands;

    public Permission perms = null;
    public WorldEditPlugin worldEdit = null;
    public ShapesLoader shapesLoader = null;

    @Override
    public void onEnable() {
        if (!setupPermissions()) {
            getLogger().severe("No Vault found. Some permissions disabled.");
        }

        if (!setupWorldedit()) {
            getLogger().severe("No WorldEdit found. Island molds disabled.");
        }

        new UpdateChecker(this, 84303).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("You are up to date.");
            } else {
                getLogger().info("There's a new update available!");
            }
        });

        getConfig().options().copyDefaults(true);
        saveConfig();

        initIslandsConfig();
        initBiomesConfig();

        islandsWorld = getIslandsWorld();
        islandsSourceWorld = getSourceWorld();
        wildernessWorld = getWilderness();

        islands = new Islands(this);

        new IslandManagmentCommands(this);

        IslandCommands islandCommands = new IslandCommands(this);

        islandCommands.new HomeCommand();
        islandCommands.new VisitCommand();

        TrustCommands trustCommands = new TrustCommands(this);

        trustCommands.new UntrustCommand();
        trustCommands.new TrustCommand();
        trustCommands.new ListTrustedCommand();

        new IslandsListener(this);

        int pluginId = 8974;
        Metrics metrics = new Metrics(this, pluginId);

        getLogger().info("Islands enabled!");
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

    private boolean setupWorldedit() {
        worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (worldEdit instanceof WorldEditPlugin) {
            shapesLoader = new ShapesLoader(this);
            return true;
        } else return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
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

    public FileConfiguration getBiomesConfig() {
        return this.biomesConfig;
    }

    public void saveBiomesConfig() {
        try {
            biomesConfig.save(biomesConfigFile);
        } catch (IOException e) {
            getLogger().severe("Unable to save biomesConfig");
        }
    }

    public void clearBiomesConfig() {
        biomesConfigFile.delete();
        initBiomesConfig();
    }

    private void initBiomesConfig() {
        biomesConfigFile = new File(getDataFolder(), "biomeCache.yml");
        if (!biomesConfigFile.exists()) {
            biomesConfigFile.getParentFile().mkdirs();
            saveResource("biomeCache.yml", false);
         }

        biomesConfig = new YamlConfiguration();
        try {
            biomesConfig.load(biomesConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
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
}
