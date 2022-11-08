package me.aleksilassila.islands;

import me.aleksilassila.islands.utils.ChatUtils;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.TimeSkipEvent;

import java.util.Date;

public class Listeners extends ChatUtils implements Listener {
    private final Plugin plugin;
    private final Islands islands;

    private final Config config;

    private final World islandsWorld;
    private final World sourceWorld;
    private final World wildernessWorld;

    public Listeners(Plugin plugin) {
        this.plugin = Plugin.instance;
        this.islands = Plugin.islands;
        this.config = islands.config;

        this.islandsWorld = islands.getIslandsWorld();
        this.sourceWorld = islands.getSourceWorld();
        this.wildernessWorld = islands.getWildernessWorld();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            if (islands.islandsConfig.spawnIsland != null) {
                islands.islandsConfig.spawnIsland.teleport(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.isBedSpawn() && !config.overrideBedSpawns) return;
        if (islands.islandsConfig.spawnIsland != null) {
            event.setRespawnLocation(islands.islandsConfig.spawnIsland.getIslandSpawn());
            if (config.islandDamage) islands.playersWithNoFall.add(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPortalEvent(PlayerPortalEvent event) {
        if (event.getTo() != null && islandsWorld.equals(event.getTo().getWorld()) && wildernessWorld != null) {
            Location to = event.getTo();
            to.setWorld(wildernessWorld);
            event.setTo(to);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL) && event.getEntity().getWorld().equals(islandsWorld) && config.disableMobs) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!event.getBlock().getWorld().equals(islandsWorld) || !config.restrictFlow) return;
        boolean canFlow = islands.islandsConfig.isBlockInWaterFlowArea(
                event.getToBlock().getX(), event.getToBlock().getY(), event.getToBlock().getZ());

        if (!canFlow) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Player teleportation in void, damage restrictions
    public void onDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID) && player.getWorld().equals(islandsWorld) && config.voidTeleport) {
                World targetWorld;

                if (wildernessWorld == null) {
                    targetWorld = plugin.getServer().getWorlds().get(0);
                } else {
                    targetWorld = wildernessWorld;
                }

                Location location;
                if (config.preserveWildernessPositions && islands.wildernessWorld.wildernessPositions.containsKey(player)) {
                    location = islands.wildernessWorld.wildernessPositions.get(player);
                } else {
                    location = player.getLocation();

                    int teleportMultiplier = plugin.getConfig().getInt("wildernessCoordinateMultiplier") <= 0
                            ? 4 : plugin.getConfig().getInt("wildernessCoordinateMultiplier");

                    location.setX(location.getBlockX() * teleportMultiplier);
                    location.setZ(location.getBlockZ() * teleportMultiplier);
                }

                location.setWorld(targetWorld);
                location.setY(targetWorld.getHighestBlockYAt(location) + 40);

                player.teleport(location);

                player.sendTitle(Messages.get("success.WILDERNESS_TELEPORT_TITLE"),
                        Messages.get("success.WILDERNESS_TELEPORT_SUBTITLE"), (int) (20 * 0.5), 20 * 5, (int) (20 * 0.5));

                islands.playersWithNoFall.add(player);

                e.setCancelled(true);
            } else if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL) && islands.playersWithNoFall.contains(player)) {
                islands.playersWithNoFall.remove(player);
                e.setCancelled(true);
            } else if (player.getWorld().equals(islandsWorld) && !config.islandDamage) {
                e.setCancelled(true);
            } else {
                islands.teleportCooldowns.put(player.getUniqueId().toString(), new Date().getTime());
            }
        }
    }

    @EventHandler
    private void checkIfPlayerLandsInWater(PlayerMoveEvent event) {
        Location l;
        if (event.getTo() == null) return;
        else l = event.getTo();

        if (islands.playersWithNoFall.contains(event.getPlayer())) {
            if (l.getWorld() == wildernessWorld || (config.islandDamage && l.getWorld() == islandsWorld))
                if (l.getBlock().isLiquid()) islands.playersWithNoFall.remove(event.getPlayer());
        }
    }

    // Sync clocks
    @EventHandler
    private void onTimeSkip(TimeSkipEvent event) {
        if (!config.syncTime) return;
        if (!event.getSkipReason().equals(TimeSkipEvent.SkipReason.NIGHT_SKIP)) return;

        long targetTime = event.getWorld().getTime() + event.getSkipAmount();

        if (event.getWorld().equals(islandsWorld)) {
            wildernessWorld.setTime(targetTime);
        } else if (event.getWorld().equals(wildernessWorld)) {
            islandsWorld.setTime(targetTime);
        }
    }

    @EventHandler
    private void onWorldChange(PlayerTeleportEvent event) {
        if (!config.preserveWildernessPositions) return;
        if (wildernessWorld == event.getFrom().getWorld())
            if (event.getTo() != null && wildernessWorld != event.getTo().getWorld()) {
                islands.wildernessWorld.wildernessPositions.put(event.getPlayer(), event.getFrom());
            }
    }
}
