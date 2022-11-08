package me.aleksilassila.islands.utils;

import me.aleksilassila.islands.Plugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    private final Plugin plugin;
    private final int resourceId;

    public UpdateChecker(Plugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void checkForUpdates() {
        getVersion(version -> {
            String majorVersion = version.substring(0, version.lastIndexOf("."));
            String thisMajorVersion = plugin.getDescription().getVersion().substring(0, plugin.getDescription().getVersion().lastIndexOf("."));

            if (plugin.getDescription().getVersion().equalsIgnoreCase(version)) {
                plugin.getLogger().info("You are up to date.");
            } else if (!majorVersion.equalsIgnoreCase(thisMajorVersion)) {
                plugin.getLogger().warning("There's a new major update available!");
            } else {
                plugin.getLogger().info("There's a new minor update available!");
            }
        });
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                plugin.getLogger().info("Cannot look for updates: " + exception.getMessage());
            }
        });
    }
}
