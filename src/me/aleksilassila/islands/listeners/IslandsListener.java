package me.aleksilassila.islands.listeners;

import me.aleksilassila.islands.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class IslandsListener implements Listener {
    private Main plugin;

    public IslandsListener(Main plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        boolean canFlow = plugin.islands.grid.isBlockInIslandSphere(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());

        if(!canFlow) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID) && player.getWorld().getName().equals("islands")) {
                World targetWorld = Bukkit.getWorld("world");

                Location location = player.getLocation();
                location.setWorld(targetWorld);

                location.setX(location.getBlockX() * 4);
                location.setZ(location.getBlockZ() * 4);
                location.setY(targetWorld.getHighestBlockYAt(location) + 40);

                player.teleport(location);

                player.sendTitle("", ChatColor.GOLD + "Type /home to get back to your island.", (int)(20 * 0.5), 20 * 5, (int)(20 * 0.5));

                plugin.islands.playersWithNoFall.add(player);

                e.setCancelled(true);
            } else if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                if (plugin.islands.playersWithNoFall.contains(player)) {
                    plugin.islands.playersWithNoFall.remove(player);
                    e.setCancelled(true);
                } else if (player.getWorld().getName().equals("islands")) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
