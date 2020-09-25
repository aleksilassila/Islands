package me.aleksilassila.islands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.aleksilassila.islands.commands.GoCommand;
import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.generation.EmptyWorldGenerator;
import me.aleksilassila.islands.listeners.IslandsListener;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

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

    @Override
    public void onEnable() {
        if (getWorldEdit() == null) {
            Bukkit.getLogger().severe("No WorldEdit found. You might run into errors.");
        }

        if (!setupPermissions()) {
            Bukkit.getLogger().severe("No Vault found. Permissions disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getConfig().options().copyDefaults(true);
        saveConfig();

        initIslandsConfig();
        initBiomesConfig();

        islandsWorld = Bukkit.getWorlds().get(0);
        islandsSourceWorld = getSourceWorld();
        wildernessWorld = getWilderness();

        islands = new Islands(islandsWorld, islandsSourceWorld, this);

        new IslandManagmentCommands(this);

        IslandCommands islandCommands = new IslandCommands(this);

        islandCommands.new HomeCommand();
        islandCommands.new VisitCommand();
        islandCommands.new TrustCommand();
        islandCommands.new UntrustCommand();

        new GoCommand(this);
        new IslandsListener(this);

        getLogger().info("Islands enabled!");
    }

    public static WorldEditPlugin getWorldEdit() {
        return (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
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

    World getSourceWorld() {
        Bukkit.getServer().getLogger().info("Creating islands source world...");

        WorldCreator wc = new WorldCreator("islandsSource");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.NORMAL);
        wc.generateStructures(false);

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.PEACEFUL);

        return world;
    }

    World getWilderness() {
        Bukkit.getServer().getLogger().info("Creating wilderness...");

        WorldCreator wc = new WorldCreator("wilderness");

        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.NORMAL);

        World world = wc.createWorld();

        world.setDifficulty(Difficulty.HARD);

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
