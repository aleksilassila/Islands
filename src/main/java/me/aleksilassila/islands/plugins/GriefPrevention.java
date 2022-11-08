package me.aleksilassila.islands.plugins;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;
import me.ryanhamshire.GriefPrevention.ClaimsMode;
import org.bukkit.World;

import java.util.concurrent.ConcurrentHashMap;

public class GriefPrevention extends AbstractPlugin {
    public final me.ryanhamshire.GriefPrevention.GriefPrevention griefPrevention;

    public GriefPrevention(Islands islands) {
        super(islands);

        if (!plugin.getConfig().getBoolean("enableIslandProtection", true)) {
            this.griefPrevention = null;
            return;
        }

        this.griefPrevention = (me.ryanhamshire.GriefPrevention.GriefPrevention) plugin.getServer().getPluginManager().getPlugin("GriefPrevention");

        if (!isEnabled()) {
            plugin.getLogger().severe("No GriefPrevention found. Island protection disabled.");
        } else if (plugin.getConfig().getBoolean("overrideGriefPreventionWorlds") && this.griefPrevention != null) {
            ConcurrentHashMap<World, ClaimsMode> modes = griefPrevention.config_claims_worldModes;
            modes.put(Plugin.islands.islandsWorld.getWorld(), ClaimsMode.SurvivalRequiringClaims);

            if (Plugin.islands.islandsWorld.getWorld() != null)
                modes.put(Plugin.islands.islandsWorld.getWorld(), ClaimsMode.Survival);
        }
    }

    @Override
    protected String getPluginName() {
        return "GriefPrevention";
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && griefPrevention != null;
    }
}
