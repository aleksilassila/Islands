package me.aleksilassila.islands.plugins;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.aleksilassila.islands.Islands;
import org.bukkit.plugin.RegisteredServiceProvider;

public class WorldEdit extends AbstractPlugin {
    private final WorldEditPlugin worldEdit;

    public WorldEdit(Islands islands) {
        super(islands);

        RegisteredServiceProvider<WorldEditPlugin> worldEditProvider = plugin.getServer().getServicesManager().getRegistration(WorldEditPlugin.class);
        this.worldEdit = worldEditProvider == null ? null : worldEditProvider.getProvider();

        if (!isEnabled()) {
            plugin.getLogger().severe("No WorldEdit found. Island saving to schematic files disabled.");
        }
    }

    @Override
    protected String getPluginName() {
        return "WorldEdit";
    }

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && worldEdit != null;
    }
}
