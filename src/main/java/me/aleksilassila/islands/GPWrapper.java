package me.aleksilassila.islands;

import me.ryanhamshire.GriefPrevention.ClaimsMode;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.concurrent.ConcurrentHashMap;

public class GPWrapper {
    public static boolean enabled = false;
    public static GriefPrevention gp;

    private static Islands islands;

    public static void initialise() {
        islands = Islands.instance;
        enabled = islands.getConfig().getBoolean("enableIslandProtection", true);
        if (!enabled) return;
        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") == null) {
            islands.getLogger().severe("No GriefPrevention found. Island protection disabled.");
            enabled = false;
        } else {
            gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
        }

        if (enabled && islands.getConfig().getBoolean("overrideGriefPreventionWorlds")) {
            ConcurrentHashMap<World, ClaimsMode> modes = gp.config_claims_worldModes;

            modes.put(Islands.islandsWorld, ClaimsMode.SurvivalRequiringClaims);

            if (Islands.wildernessWorld != null)
                modes.put(Islands.wildernessWorld, ClaimsMode.Survival);
        }
    }
}
