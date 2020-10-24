package me.aleksilassila.islands.listeners;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.ChatUtils;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Date;

public class IslandsListener extends ChatUtils implements Listener {
    private final Islands plugin;

    private final boolean disableMobs;
    private final boolean voidTeleport;
    private final boolean islandDamage;
    private final boolean restrictFlow;

    public IslandsListener(Islands plugin) {
        this.plugin = plugin;

        this.voidTeleport = !plugin.getConfig().getKeys(false).contains("voidTeleport")
                || plugin.getConfig().getBoolean("voidTeleport");
        this.restrictFlow = plugin.getConfig().getKeys(false).contains("restrictIslandBlockFlows")
                || plugin.getConfig().getBoolean("restrictIslandBlockFlows");
        this.disableMobs = plugin.getConfig().getBoolean("disableMobsOnIslands");
        this.islandDamage = plugin.getConfig().getBoolean("islandDamage");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            String spawnIsland = plugin.layout.getSpawnIsland();

            if (spawnIsland != null) {
                event.getPlayer().teleport(plugin.layout.getIslandSpawn(spawnIsland));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String spawnIsland = plugin.layout.getSpawnIsland();

        if (spawnIsland != null) {
            event.setRespawnLocation(plugin.layout.getIslandSpawn(spawnIsland));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPortalEvent(PlayerPortalEvent event) {
        if (event.getTo().getWorld().equals(plugin.islandsWorld)) {
            Location to = event.getTo();
            to.setWorld(plugin.wildernessWorld);
            event.setTo(to);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL) && event.getEntity().getWorld().equals(plugin.islandsWorld) && disableMobs) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!event.getBlock().getWorld().equals(plugin.islandsWorld) || !restrictFlow) return;
        boolean canFlow = plugin.layout.isBlockInIslandSphere(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());

        if(!canFlow) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Player teleportation in void, damage restrictions
    public void onDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID) && player.getWorld().equals(plugin.islandsWorld) && voidTeleport) {
                World targetWorld = plugin.wildernessWorld;

                Location location = player.getLocation();
                location.setWorld(targetWorld);

                int teleportMultiplier = plugin.getConfig().getInt("wildernessCoordinateMultiplier") <= 0
                        ? 4 : plugin.getConfig().getInt("wildernessCoordinateMultiplier");

                location.setX(location.getBlockX() * teleportMultiplier);
                location.setZ(location.getBlockZ() * teleportMultiplier);
                location.setY(targetWorld.getHighestBlockYAt(location) + 40);

                player.teleport(location);

                player.sendTitle("", ChatColor.GOLD + "Type /home to get back to your island.", (int)(20 * 0.5), 20 * 5, (int)(20 * 0.5));

                plugin.playersWithNoFall.add(player);

                e.setCancelled(true);
            } else if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL) && plugin.playersWithNoFall.contains(player)) {
                plugin.playersWithNoFall.remove(player);
                e.setCancelled(true);
            } else if (player.getWorld().equals(plugin.islandsWorld) && !islandDamage) {
                e.setCancelled(true);
            } else {
                plugin.teleportCooldowns.put(player.getUniqueId().toString(), new Date().getTime());
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent e) {
         if (e.getEntity().getWorld().equals(plugin.islandsWorld) && e.getDamager() instanceof Player) {
             if (e.getDamager().hasPermission(Permissions.bypass.interactEverywhere)) return;

             int x = e.getEntity().getLocation().getBlockX();
             int z = e.getEntity().getLocation().getBlockZ();

             String ownerUUID = plugin.layout.getBlockOwnerUUID(x, z);
             if (ownerUUID != null && !ownerUUID.equals(e.getDamager().getUniqueId().toString())) {
                 if (plugin.layout.getTrusted(plugin.layout.getIslandId(x, z)).contains(e.getDamager().getUniqueId().toString())) {
                     return;
                 }

                 e.setCancelled(true);

                 e.getDamager().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
    }

    @EventHandler // Player interact restriction
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) return;

        if (event.getPlayer().getWorld().equals(plugin.islandsWorld)) {
            int x = event.getClickedBlock().getX();
            int z = event.getClickedBlock().getZ();

            String ownerUUID = plugin.layout.getBlockOwnerUUID(x, z);

            if (ownerUUID == null || !ownerUUID.equals(event.getPlayer().getUniqueId().toString())) {
                if (plugin.layout.getTrusted(plugin.layout.getIslandId(x, z)).contains(event.getPlayer().getUniqueId().toString())) {
                    return;
                }

                event.setCancelled(true);

                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
    }

    // Above only checks if  the block clicked is in build radius, allowing block placement in restricted areas.
    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) return;

        if (event.getBlock().getWorld().equals(plugin.islandsWorld)) {
            int x = event.getBlock().getX();
            int z = event.getBlock().getZ();

            String ownerUUID = plugin.layout.getBlockOwnerUUID(x, z);

            if (ownerUUID == null || !ownerUUID.equals(event.getPlayer().getUniqueId().toString())) {
                if (plugin.layout.getTrusted(plugin.layout.getIslandId(x, z)).contains(event.getPlayer().getUniqueId().toString())) {
                    return;
                }

                event.setCancelled(true);

                if (ownerUUID != null) event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }

        }
    }
}
