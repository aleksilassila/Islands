package me.aleksilassila.islands.protection;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class ProtectionListeners implements Listener {
    public ProtectionListeners() {
        Islands.instance.getServer().getPluginManager().registerEvents(this, Islands.instance);
    }

    @EventHandler
    private void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || !event.getEntity().getWorld().equals(Islands.islandsWorld)) return;

        Player player;

        if (event.getDamager() instanceof Player) player = (Player) event.getDamager();
        else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player) player = (Player) arrow.getShooter();
            else return;
        } else return;

        if (!canBuild(player, event.getEntity().getLocation().getBlockX(),
                event.getEntity().getLocation().getBlockZ())) {
            event.setCancelled(true);
            player.sendMessage(Messages.get("error.NOT_TRUSTED"));
        }
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;

        if (event.getPlayer().getWorld().equals(Islands.islandsWorld)) {
            if (event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) return;

            int x = event.getRightClicked().getLocation().getBlockX();
            int z = event.getRightClicked().getLocation().getBlockZ();

            String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z, event.getPlayer().hasPermission(Permissions.bypass.interactInPlot));
            if (ownerUUID != null && !ownerUUID.equals(event.getPlayer().getUniqueId().toString())) {
                if (event.getRightClicked().getType() == EntityType.MINECART) {
                    if (new ProtectedBlock(x, z).canUseUtility(event.getPlayer().getUniqueId().toString())) return;
                }
                else if (new ProtectedBlock(x, z).canOpenContainers(event.getPlayer().getUniqueId().toString())) return; // fixme armorstands work but what about others

                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        onPlayerInteractEntity(event);
    }

    @EventHandler
    private void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        onPlayerBucketEvent(event);
    }

    @EventHandler
    private void onPlayerBucketFill(PlayerBucketFillEvent event) {
        onPlayerBucketEvent(event);
    }

    private void onPlayerBucketEvent(PlayerBucketEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().getWorld().equals(Islands.islandsWorld)) {
            if (!canBuild(event.getPlayer(), event.getBlock().getLocation().getBlockX(),
                    event.getBlock().getLocation().getBlockZ())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
    }

    @EventHandler // Player interact restriction
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) return;
        if (!event.getPlayer().getWorld().equals(Islands.islandsWorld)) return;

        Block clickedBlock = event.getClickedBlock();
        int x = clickedBlock.getX();
        int z = clickedBlock.getZ();

        Player player = event.getPlayer();
        String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z, player.hasPermission(Permissions.bypass.interactInPlot));

        if (player.getUniqueId().toString().equalsIgnoreCase(ownerUUID)) return;

        ProtectedBlock protectedBlock = new ProtectedBlock(x, z);

        // Handle building access
        if (protectedBlock.canDoAnything(player.getUniqueId().toString())) return;

        if (event.getAction() == Action.PHYSICAL) {
            if (Tag.PRESSURE_PLATES.isTagged(clickedBlock.getType()) &&
                    !protectedBlock.canUseDoors(player.getUniqueId().toString())) {
                event.setCancelled(true);
            } else if (clickedBlock.getType() == Material.FARMLAND &&
                    !protectedBlock.canDoAnything(player.getUniqueId().toString())) {
                event.setCancelled(true);
            }
        }

        // These need permission to do anything
        if (clickedBlock.getType() == Material.TNT ||
                Tag.RAILS.isTagged(clickedBlock.getType()) ||
                (event.getItem() != null && event.getItem().getType() == Material.BONE_MEAL) ||
                (event.getItem() != null && event.getItem().getType() == Material.ARMOR_STAND)) {
            event.setCancelled(true);
            Messages.send(event.getPlayer(), "error.NOT_TRUSTED");
            return;
        }

        // Doors
        if (Tag.WOODEN_DOORS.isTagged(clickedBlock.getType()) ||
                Tag.WOODEN_TRAPDOORS.isTagged(clickedBlock.getType()) ||
                Tag.FENCE_GATES.isTagged(clickedBlock.getType()))
        {
            if (!protectedBlock.canUseDoors(player.getUniqueId().toString())) {
                event.setCancelled(true);
                Messages.send(event.getPlayer(), "error.NOT_TRUSTED");
            }
        }

        // Containers
        else if (clickedBlock.getType() == Material.CHEST ||
                clickedBlock.getType() == Material.TRAPPED_CHEST ||
                clickedBlock.getType() == Material.CHEST_MINECART ||
                clickedBlock.getType() == Material.FURNACE ||
                clickedBlock.getType() == Material.BLAST_FURNACE ||
                clickedBlock.getType() == Material.FURNACE_MINECART ||
                clickedBlock.getType() == Material.HOPPER ||
                clickedBlock.getType() == Material.HOPPER_MINECART ||
                clickedBlock.getType() == Material.DISPENSER ||
                clickedBlock.getType() == Material.DROPPER ||
                clickedBlock.getType() == Material.JUKEBOX ||
                clickedBlock.getType() == Material.BARREL ||
                clickedBlock.getType() == Material.SMOKER ||
                Tag.SHULKER_BOXES.isTagged(clickedBlock.getType()))
        {
            if (!protectedBlock.canOpenContainers(player.getUniqueId().toString())) {
                event.setCancelled(true);
                Messages.send(event.getPlayer(), "error.NOT_TRUSTED");
            }
        }
        // Utility
        else if (Tag.CAMPFIRES.isTagged(clickedBlock.getType()) ||
                Tag.BEDS.isTagged(clickedBlock.getType()) ||
                clickedBlock.getType() == Material.NOTE_BLOCK ||
                clickedBlock.getType() == Material.CAULDRON ||
                clickedBlock.getType() == Material.ANVIL ||
                clickedBlock.getType() == Material.CHIPPED_ANVIL ||
                clickedBlock.getType() == Material.DAMAGED_ANVIL ||
                clickedBlock.getType() == Material.CAKE ||
                clickedBlock.getType() == Material.SWEET_BERRY_BUSH ||
                clickedBlock.getType() == Material.BEE_NEST ||
                clickedBlock.getType() == Material.BEEHIVE ||
                clickedBlock.getType() == Material.BEACON ||
                clickedBlock.getType() == Material.BELL ||
                clickedBlock.getType() == Material.CARTOGRAPHY_TABLE ||
                clickedBlock.getType() == Material.LOOM ||
                clickedBlock.getType() == Material.MINECART ||
                clickedBlock.getType() == Material.RESPAWN_ANCHOR)
        {
            if (!protectedBlock.canUseUtility(player.getUniqueId().toString())) {
                event.setCancelled(true);
                Messages.send(event.getPlayer(), "error.NOT_TRUSTED");
            }
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) return;

        if (event.getBlock().getWorld().equals(Islands.islandsWorld)) {
            int x = event.getBlock().getX();
            int z = event.getBlock().getZ();

            String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z, event.getPlayer().hasPermission(Permissions.bypass.interactInPlot));

            if (ownerUUID == null && !event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) {
                event.setCancelled(true);
            } else if (!(new ProtectedBlock(x, z).canDoAnything(event.getPlayer().getUniqueId().toString()))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }

        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) return;

        if (event.getBlock().getWorld().equals(Islands.islandsWorld)) {
            int x = event.getBlock().getX();
            int z = event.getBlock().getZ();

            String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z, event.getPlayer().hasPermission(Permissions.bypass.interactInPlot));

            if (ownerUUID == null && !event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) {
                event.setCancelled(true);
            } else if (!(new ProtectedBlock(x, z).canDoAnything(event.getPlayer().getUniqueId().toString()))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
    }

    // MINECARTS
    @EventHandler
    private void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getAttacker() instanceof Player)) return;

        Player player = (Player) event.getAttacker();
        if (event.getVehicle().getWorld().equals(Islands.islandsWorld)) {
            if (!canBuild(player, event.getVehicle().getLocation().getBlockX(),
                    event.getVehicle().getLocation().getBlockZ())) {

                event.setCancelled(true);
                player.sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
    }

    // ARROWS AND TNT IGNITION
    @EventHandler
    private void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.isCancelled() || !Islands.islandsWorld.equals(event.getBlock().getWorld())) return;

        if (event.getEntity() instanceof Arrow &&
                event.getBlock().getType() == Material.TNT) {
            Arrow arrow = (Arrow) event.getEntity();
            if (arrow.getShooter() instanceof Player) {
                Player owner = (Player) arrow.getShooter();

                if (!canBuild(owner, event.getBlock().getX(), event.getBlock().getZ())) event.setCancelled(true);
            }
        }
    }

    public boolean canBuild(Player player, int x, int z) {
        if (player.hasPermission(Permissions.bypass.interactEverywhere)) return true;

        String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z, player.hasPermission(Permissions.bypass.interactInPlot));
        if (ownerUUID == null) return false;
        if (!ownerUUID.equals(player.getUniqueId().toString())) {
            return new ProtectedBlock(x, z).canDoAnything(player.getUniqueId().toString());
        }

        return true;
    }
}
