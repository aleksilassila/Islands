package me.aleksilassila.islands;

import me.aleksilassila.islands.commands.IslandCommands;
import me.aleksilassila.islands.commands.IslandManagmentCommands;
import me.aleksilassila.islands.commands.TrustCommands;
import me.aleksilassila.islands.generation.EmptyWorldGenerator;
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
        if (!setupPermissions()) {
            Bukkit.getLogger().severe("No Vault found. Some permissions disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new UpdateChecker(this, 84303).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                Bukkit.getLogger().info("[Islands] You are up to date.");
            } else {
                Bukkit.getLogger().info("[Islands] There's a new update available!");
            }
        });

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

        TrustCommands trustCommands = new TrustCommands(this);

        trustCommands.new UntrustCommand();
        trustCommands.new TrustCommand();
        trustCommands.new ListTrustedCommand();

        new IslandsListener(this);

        int pluginId = 8974; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        getLogger().info("Islands enabled!");
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            perms = rsp.getProvider();
            return true;
        }
        return false;
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
