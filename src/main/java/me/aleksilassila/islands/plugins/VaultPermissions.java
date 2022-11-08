package me.aleksilassila.islands.plugins;

import me.aleksilassila.islands.Islands;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPermissions extends AbstractPlugin {
    private final Permission permissions;

    public VaultPermissions(Islands islands) {
        super(islands);

        RegisteredServiceProvider<Permission> permissionProvider = null;

        try {
            permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        } catch (NoClassDefFoundError ignored) {
        }

        permissions = permissionProvider == null ? null : permissionProvider.getProvider();

        if (!isEnabled()) {
            plugin.getLogger().severe("No Vault found. Some permissions disabled.");
        }
    }

    @Override
    protected String getPluginName() {
        return "vault";
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && permissions != null;
    }

    public Permission getPermissions() {
        return permissions;
    }
}
