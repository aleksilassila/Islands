package me.aleksilassila.islands.listeners;

import me.aleksilassila.islands.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class IslandVisitGuiHandler implements Listener {
    private Inventory inv;
    private List<String> islandIds;
    private final Main plugin;
    private Map<String, Map<String, String>> publicIslands;

    private final int inventorySize = 9 * 6;

    public IslandVisitGuiHandler(Main plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // You can call this whenever you want to put the items in
    private void initializeItems() {
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        inv = Bukkit.createInventory(null, 9 * 6, "Visit Island");
        islandIds = new ArrayList<>();
        publicIslands = plugin.islands.layout.getPublicIslands();


        for (String islandId : publicIslands.keySet()) {
            if (islandIds.size() >= inventorySize) break;
            Player player = Bukkit.getPlayer(UUID.fromString(publicIslands.get(islandId).get("owner")));
            inv.addItem(createGuiItem(Material.GRASS_BLOCK, ChatColor.GOLD + publicIslands.get(islandId).get("name"), ChatColor.GRAY + "By " + (player != null ? player.getDisplayName() : "unknown")));
            islandIds.add(islandId);
        }
    }

    // Nice little method to create a gui item with a custom name, and description
    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }

    // You can open the inventory with this
    public void openVisitGui(final HumanEntity ent) {
        initializeItems();
        ent.openInventory(inv);
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory() != inv) return;

        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        if (publicIslands == null) return;

        final Player p = (Player) event.getWhoClicked();

        p.performCommand("visit " + publicIslands.get(islandIds.get(event.getRawSlot())).get("name"));
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory() == inv) {
          e.setCancelled(true);
        }
    }
}
