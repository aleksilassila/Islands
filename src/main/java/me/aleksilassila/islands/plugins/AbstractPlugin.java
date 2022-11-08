package me.aleksilassila.islands.plugins;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Plugin;

abstract public class AbstractPlugin {
    protected Plugin plugin;
    protected final org.bukkit.plugin.Plugin bukkitPlugin;

    public AbstractPlugin(Islands islands) {
        this.plugin = islands.plugin;
        this.bukkitPlugin = plugin.getServer().getPluginManager().getPlugin(getPluginName());
    }

    protected abstract String getPluginName();

    public org.bukkit.plugin.Plugin getPlugin() {
        return bukkitPlugin;
    }

    public boolean isEnabled() {
        return bukkitPlugin != null;
    }

}
