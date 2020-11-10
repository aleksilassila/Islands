package me.aleksilassila.islands;

import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.TimeSkipEvent;

import java.util.Date;

public class Listeners extends ChatUtils implements Listener {
    private final Islands plugin;

    private final boolean disableMobs;
    private final boolean voidTeleport;
    private final boolean islandDamage;
    private final boolean restrictFlow;
    private final boolean syncTime;

    public Listeners() {
        this.plugin = Islands.instance;

        this.voidTeleport = plugin.getConfig().getBoolean("voidTeleport");
        this.restrictFlow = plugin.getConfig().getBoolean("restrictIslandBlockFlows");
        this.disableMobs = plugin.getConfig().getBoolean("disableMobsOnIslands");
        this.islandDamage = plugin.getConfig().getBoolean("islandDamage");
        this.syncTime = plugin.getConfig().getBoolean("syncTime");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            String spawnIsland = IslandsConfig.getSpawnIsland();

            if (spawnIsland != null) {
                event.getPlayer().teleport(IslandsConfig.getIslandSpawn(spawnIsland));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String spawnIsland = IslandsConfig.getSpawnIsland();

        if (spawnIsland != null) {
            event.setRespawnLocation(IslandsConfig.getIslandSpawn(spawnIsland));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPortalEvent(PlayerPortalEvent event) {
        if (event.getTo() != null && Islands.islandsWorld.equals(event.getTo().getWorld()) && Islands.wildernessWorld != null) {
            Location to = event.getTo();
            to.setWorld(Islands.wildernessWorld);
            event.setTo(to);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL) && event.getEntity().getWorld().equals(Islands.islandsWorld) && disableMobs) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!event.getBlock().getWorld().equals(Islands.islandsWorld) || !restrictFlow) return;
        boolean canFlow = IslandsConfig.isBlockInIslandSphere(
                event.getToBlock().getX(), event.getToBlock().getY(), event.getToBlock().getZ());

        if(!canFlow) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Player teleportation in void, damage restrictions
    public void onDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID) && player.getWorld().equals(Islands.islandsWorld) && voidTeleport) {
                World targetWorld;

                if (Islands.wildernessWorld == null) {
                    targetWorld = plugin.getServer().getWorlds().get(0);
                } else {
                    targetWorld = Islands.wildernessWorld;
                }

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
            } else if (player.getWorld().equals(Islands.islandsWorld) && !islandDamage) {
                e.setCancelled(true);
            } else {
                plugin.teleportCooldowns.put(player.getUniqueId().toString(), new Date().getTime());
            }
        }
    }

    // Sync clocks
    @EventHandler
    private void onTimeSkip(TimeSkipEvent event) {
        if (!syncTime) return;
        if (!event.getSkipReason().equals(TimeSkipEvent.SkipReason.NIGHT_SKIP)) return;

        long targetTime = event.getWorld().getTime() + event.getSkipAmount();

        if (event.getWorld().equals(Islands.islandsWorld)) {
            Islands.wildernessWorld.setTime(targetTime);
        } else if (event.getWorld().equals(Islands.wildernessWorld)) {
            Islands.islandsWorld.setTime(targetTime);
        }
    }
}
