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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class IslandVisitGuiHandler implements Listener {
    private final Map<Integer, Inventory> inventories = new HashMap<>();
    private List<String> islandIds;
    private final Main plugin;
    private Map<String, Map<String, String>> publicIslands;

    private final int inventorySize = 9 * 3;
    private final int whiteSpace = 9;
    private final int islandsOnPage = inventorySize - whiteSpace - 9;

    public IslandVisitGuiHandler(Main plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // You can call this whenever you want to put the items in
    private void updatePage(int page) {
        Inventory inv = Bukkit.createInventory(null, inventorySize, "Visit Island");
        islandIds = new ArrayList<>();
        publicIslands = plugin.islands.layout.getPublicIslands();

        islandIds.addAll(publicIslands.keySet());

        int index = 0;
        int startIndex = islandsOnPage * page;
        for (String islandId : publicIslands.keySet()) {
            if (index < startIndex || index >= startIndex + islandsOnPage) {
                index++;
                continue;
            }

            Player player = Bukkit.getPlayer(UUID.fromString(publicIslands.get(islandId).get("owner")));
            inv.addItem(createGuiItem(Material.GRASS_BLOCK, ChatColor.GOLD + publicIslands.get(islandId).get("name"), ChatColor.GRAY + "By " + (player != null ? player.getDisplayName() : "unknown")));
            index++;
        }

        inv.setItem(islandsOnPage + whiteSpace + 7, createGuiItem(Material.BOOK, ChatColor.GOLD + "Next page"));
        inv.setItem(islandsOnPage + whiteSpace + 1, createGuiItem(Material.BOOK, ChatColor.GOLD + "Previous page"));

        inventories.put(page, inv);
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
    public void openVisitGui(final HumanEntity ent, int page) {
        updatePage(page);
        ent.openInventory(inventories.get(page));
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Holder) {
            Bukkit.getLogger().info("IS INSTANCE");
        }

        if (!inventories.containsValue(event.getInventory())) return;

        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        if (publicIslands == null) return;

        final Player player = (Player) event.getWhoClicked();

        if (event.getRawSlot() < islandsOnPage) {
            player.performCommand("visit " + publicIslands.get(islandIds.get(event.getRawSlot())).get("name"));
        } else {
            int currentInventory = 0;

            for (Integer index : inventories.keySet()) {
                if (inventories.get(index).equals(event.getInventory())) {
                    currentInventory = index;
                    break;
                }
            }

            if (event.getRawSlot() - 9 - whiteSpace == 1 && currentInventory - 1 >= 0) {
                openVisitGui(player, currentInventory - 1);
            } else if (event.getRawSlot() - 9 - whiteSpace == 7) {
                openVisitGui(player, currentInventory + 1);
            }
        }

    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent event) {
        if (inventories.containsValue(event.getInventory())) {
          event.setCancelled(true);
        }
    }

    abstract class Holder implements InventoryHolder {
        Inventory inventory;

        public Holder() {
            inventory = Bukkit.createInventory(null, inventorySize, "Visit Island");
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

    }

    interface IHolder extends InventoryHolder {}
}
