package me.aleksilassila.islands.listeners;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import me.aleksilassila.islands.utils.TrustedPlayer;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionListeners implements Listener {
    private final Islands plugin;
    private final IslandLayout layout;

    public ProtectionListeners(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity().getWorld().equals(plugin.islandsWorld) && event.getDamager() instanceof Player) {
            if (event.getDamager().hasPermission(Permissions.bypass.interactEverywhere)) return;

            int x = event.getEntity().getLocation().getBlockX();
            int z = event.getEntity().getLocation().getBlockZ();

            String ownerUUID = plugin.layout.getBlockOwnerUUID(x, z);
            if (ownerUUID != null && !ownerUUID.equals(event.getDamager().getUniqueId().toString())) {
                if (plugin.layout.getTrusted(x, z, ((Player) event.getDamager()).getUniqueId().toString()).isGenerallyTrusted()) {
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
        if (!event.getPlayer().getWorld().equals(plugin.islandsWorld)) return;

        int x = event.getClickedBlock().getX();
        int z = event.getClickedBlock().getZ();

        Player player = event.getPlayer();
        String ownerUUID = plugin.layout.getBlockOwnerUUID(x, z);

        if (player.getUniqueId().toString().equalsIgnoreCase(ownerUUID)) return;

        TrustedPlayer trustedPlayer = plugin.layout.getTrusted(x, z, player.getUniqueId().toString());

        if (trustedPlayer.isGenerallyTrusted()) return;
        else if (trustedPlayer.isDoorTrusted() && (
                Tag.WOODEN_DOORS.isTagged(event.getClickedBlock().getType()) ||
                Tag.BEDS.isTagged(event.getClickedBlock().getType()) ||
                Tag.WOODEN_TRAPDOORS.isTagged(event.getClickedBlock().getType()) ||
                event.getClickedBlock().getType() == Material.LECTERN ||
                Tag.FENCE_GATES.isTagged(event.getClickedBlock().getType()))) {
            return;
        } else if (trustedPlayer.isContainerTrusted() && (
                event.getClickedBlock().getType() == Material.CHEST ||
                event.getClickedBlock().getType() == Material.TRAPPED_CHEST ||
                event.getClickedBlock().getType() == Material.SHULKER_BOX)) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(Messages.get("error.NOT_TRUSTED"));
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
                if (plugin.layout.getTrusted(x, z, event.getPlayer().getUniqueId().toString()).isGenerallyTrusted()) {
                    return;
                }

                event.setCancelled(true);

                if (ownerUUID != null) event.getPlayer().sendMessage(Messages.get("error.CANT_PLACE"));
            }

        }
    }

}
