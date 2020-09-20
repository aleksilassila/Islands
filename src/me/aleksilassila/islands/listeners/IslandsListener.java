package me.aleksilassila.islands.listeners;

import me.aleksilassila.islands.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class IslandsListener implements Listener {
    private Main plugin;

    public IslandsListener(Main plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        boolean canFlow = plugin.islands.grid.isBlockInNormalIsland(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());

        if(!canFlow) {
            event.setCancelled(true);
        }
    }
}
