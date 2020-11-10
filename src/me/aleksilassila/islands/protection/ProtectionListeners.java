package me.aleksilassila.islands.protection;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.IslandsConfig;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionListeners implements Listener {
    public ProtectionListeners() {
        Islands.instance.getServer().getPluginManager().registerEvents(this, Islands.instance);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity().getWorld().equals(Islands.islandsWorld) && event.getDamager() instanceof Player) {
            if (event.getDamager().hasPermission(Permissions.bypass.interactEverywhere)) return;

            int x = event.getEntity().getLocation().getBlockX();
            int z = event.getEntity().getLocation().getBlockZ();

            String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z);
            if (ownerUUID != null && !ownerUUID.equals(event.getDamager().getUniqueId().toString())) {
                if (new ProtectedBlock(x, z).canDoAnything(event.getDamager().getUniqueId().toString())) {
                    return;
                }

                event.setCancelled(true);

                event.getDamager().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
    }

    @EventHandler // Player interact restriction
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) return;
        if (!event.getPlayer().getWorld().equals(Islands.islandsWorld)) return;

        int x = event.getClickedBlock().getX();
        int z = event.getClickedBlock().getZ();

        Player player = event.getPlayer();
        String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z);

        if (player.getUniqueId().toString().equalsIgnoreCase(ownerUUID)) return;

        ProtectedBlock protectedBlock = new ProtectedBlock(x, z);

        // Handle building access
        if (protectedBlock.canDoAnything(player.getUniqueId().toString())) return;

        if (event.getAction() == Action.PHYSICAL) {
            if (Tag.PRESSURE_PLATES.isTagged(event.getClickedBlock().getType()) &&
                    !protectedBlock.canUseDoors(player.getUniqueId().toString())) {
                event.setCancelled(true);
            } else if (event.getClickedBlock().getType() == Material.FARMLAND &&
                    !protectedBlock.canDoAnything(player.getUniqueId().toString())) {
                event.setCancelled(true);
            }
        }

        // Doors
        if (Tag.WOODEN_DOORS.isTagged(event.getClickedBlock().getType()) ||
                Tag.WOODEN_TRAPDOORS.isTagged(event.getClickedBlock().getType()) ||
                Tag.FENCE_GATES.isTagged(event.getClickedBlock().getType()))
        {
            if (!protectedBlock.canUseDoors(player.getUniqueId().toString())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }

        // Containers
        else if (event.getClickedBlock().getType() == Material.CHEST ||
                event.getClickedBlock().getType() == Material.TRAPPED_CHEST ||
                event.getClickedBlock().getType() == Material.CHEST_MINECART ||
                event.getClickedBlock().getType() == Material.FURNACE ||
                event.getClickedBlock().getType() == Material.BLAST_FURNACE ||
                event.getClickedBlock().getType() == Material.FURNACE_MINECART ||
                event.getClickedBlock().getType() == Material.HOPPER ||
                event.getClickedBlock().getType() == Material.HOPPER_MINECART ||
                event.getClickedBlock().getType() == Material.DISPENSER ||
                event.getClickedBlock().getType() == Material.DROPPER ||
                event.getClickedBlock().getType() == Material.JUKEBOX ||
                Tag.SHULKER_BOXES.isTagged(event.getClickedBlock().getType()))
        {
            if (!protectedBlock.canOpenContainers(player.getUniqueId().toString())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
        // Utility
        else if (Tag.CAMPFIRES.isTagged(event.getClickedBlock().getType()) ||
                Tag.BEDS.isTagged(event.getClickedBlock().getType()) ||
                event.getClickedBlock().getType() == Material.NOTE_BLOCK ||
                event.getClickedBlock().getType() == Material.CAULDRON ||
                event.getClickedBlock().getType() == Material.ANVIL ||
                event.getClickedBlock().getType() == Material.CHIPPED_ANVIL ||
                event.getClickedBlock().getType() == Material.DAMAGED_ANVIL ||
                event.getClickedBlock().getType() == Material.CAKE ||
                event.getClickedBlock().getType() == Material.SWEET_BERRY_BUSH ||
                event.getClickedBlock().getType() == Material.BEE_NEST ||
                event.getClickedBlock().getType() == Material.BEEHIVE ||
                event.getClickedBlock().getType() == Material.BEACON ||
                event.getClickedBlock().getType() == Material.BELL ||
                event.getClickedBlock().getType() == Material.CARTOGRAPHY_TABLE ||
                event.getClickedBlock().getType() == Material.LOOM ||
                event.getClickedBlock().getType() == Material.MINECART ||
                event.getClickedBlock().getType() == Material.RESPAWN_ANCHOR)
        {
            if (!protectedBlock.canUseUtility(player.getUniqueId().toString())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
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

            String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z);

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

            String ownerUUID = IslandsConfig.getBlockOwnerUUID(x, z);

            if (ownerUUID == null && !event.getPlayer().hasPermission(Permissions.bypass.interactEverywhere)) {
                event.setCancelled(true);
            } else if (!(new ProtectedBlock(x, z).canDoAnything(event.getPlayer().getUniqueId().toString()))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
            }
        }
    }
}
