package me.aleksilassila.islands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.generation.EmptyWorldGenerator;
import me.aleksilassila.islands.listeners.IslandsListener;
import org.bukkit.*;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {

    public World islandsWorld;
    public World islandsSourceWorld;

    private FileConfiguration islandsConfig;
    private File islandsConfigFile;

    public Islands islands;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        initIslandsConfig();

        islandsWorld = createIslandsWorldIfNecessary();
        islandsSourceWorld = createIslandsSourceWorldIfNecessary();

        islands = new Islands(islandsWorld, islandsSourceWorld, this);

        new IslandCommands(this);
        new IslandsListener(this);

        getLogger().info("Islands enabled!");
    }

    public static WorldEditPlugin getWorldEdit() {
        return (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
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
            getLogger().warning("Unable to save islandsConfig");
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

    World createIslandsSourceWorldIfNecessary() {
        Bukkit.getServer().getLogger().info("Creating islands source world...");

        WorldCreator wc = new WorldCreator("islandsSource");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.NORMAL);
        wc.generateStructures(false);

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.PEACEFUL);

        return world;
    }

    World createIslandsWorldIfNecessary() {
        Bukkit.getServer().getLogger().info("Creating islands world...");

        WorldCreator wc = new WorldCreator("islands");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generator(new EmptyWorldGenerator());

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.PEACEFUL);

        return world;
    }
}
