package me.aleksilassila.islands.plugins;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Economy extends AbstractPlugin {
    @Nullable
    private final net.milkbowl.vault.economy.Economy economy;
    public Map<Integer, Double> islandPrices = new HashMap<>();

    public Economy(Islands islands) {
        super(islands);

        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> provider = null;

        try {
            provider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        } catch (NoClassDefFoundError ignored) {
        }

        economy = provider == null ? null : provider.getProvider();

        if (!isEnabled()) {
            plugin.getLogger().severe("No Vault or economy plugin found. Economy disabled.");
        }

        FileConfiguration config = plugin.getConfig();

        for (String size : Objects.requireNonNull(config.getConfigurationSection("islandSizes")).getKeys(false)) {
            if (config.getDouble("islandPrices." + size) > 0) {
                islandPrices.put(config.getInt("islandSizes." + size), config.getDouble("islandPrices." + size));
            }
        }
    }

    @Override
    protected String getPluginName() {
        return "Vault";
    }

    public net.milkbowl.vault.economy.Economy getEconomy() {
        return economy;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && economy != null;
    }

    public boolean hasFunds(Player player, double cost) {
        if (economy == null || player.hasPermission("islands.bypass.economy")) return true;

        return economy.has(player, cost);
    }

    public void pay(Player player, double cost) {
        if (economy == null || player.hasPermission("islands.bypass.economy")) return;

        if (cost > 0) {
            economy.withdrawPlayer(player, cost);
            player.sendMessage(Messages.get("success.ISLAND_PURCHASED", cost));
        }
    }

    public boolean canCreate(Player player, int islandSize) {
        if (economy == null) return true;
        boolean canCreate = hasFunds(player, islandPrices.getOrDefault(islandSize, 0.0));

        if (!canCreate) {
            player.sendMessage(Messages.get("error.INSUFFICIENT_FUNDS"));
            return false;
        }

        return true;
    }
}
