package me.aleksilassila.islands.commands.GUIs;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VisitGui implements IVisitGui {
    private Inventory inventory;
    private final Main plugin;

    private final int inventorySize = 9 * 2;
    private final int whiteSpace = 0;
    private final int islandsOnPage = inventorySize - whiteSpace - 9;

    private int page = 0;
    private int sort = 0;

    public VisitGui(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory() {
        return getInventoryPage();
    }

    @Override
    public void onInventoryClick(Player player, int slot, ItemStack clickedItem, InventoryView inventoryView) {
        if(clickedItem == null || clickedItem.getType().equals(Material.AIR))
            return;

        if (slot < islandsOnPage) {
            player.closeInventory();
            player.performCommand("visit " +  ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));
        } else if (slot - 9 - whiteSpace == 0) {
            player.openInventory(this.setPage(parsePage(inventoryView.getTitle()) - 1).getInventory());
        } else if (slot - 9 - whiteSpace == 8) {
            player.openInventory(this.setPage(parsePage(inventoryView.getTitle()) + 1).getInventory());
        } else if (slot - 9 - whiteSpace == 4) {
            int i = parseSort(inventoryView.getTitle());
            player.openInventory(this.setPage(parsePage(inventoryView.getTitle())).setSort(i == 0 ? 1 : 0).getInventory());
        }
    }

    private Inventory getInventoryPage() {
        Inventory inv = Bukkit.createInventory(this, inventorySize, "Visit Island - By " + parseSort(sort) + " - [" + page + "]");
        Map<String, Map<String, String>> publicIslands = plugin.islands.layout.getPublicIslands();

        List<String> sortedSet = new ArrayList<>(publicIslands.keySet());

        if (sort == 1) { // Sort by date, oldest first
            sortedSet.sort(Comparator.comparingInt(a -> IslandLayout.placement.getIslandIndex(new int[]{Integer.parseInt(a.split("x")[0]), Integer.parseInt(a.split("x")[1])})));
        } else { // Sort by name
            sortedSet.sort(Comparator.comparingInt(a -> publicIslands.get(a).get("name").charAt(0)));
        }

        int index = 0;
        int startIndex = islandsOnPage * page;
        for (String islandId : sortedSet) {
            if (index < startIndex || index >= startIndex + islandsOnPage) {
                index++;
                continue;
            }

            Player player = Bukkit.getPlayer(UUID.fromString(publicIslands.get(islandId).get("owner")));
            inv.addItem(createGuiItem(Material.GRASS_BLOCK, ChatColor.GOLD + publicIslands.get(islandId).get("name"), ChatColor.GRAY + "By " + (player != null ? player.getDisplayName() : "unknown")));
            index++;
        }
        if ((page + 1) * islandsOnPage < publicIslands.size()) {
            inv.setItem(islandsOnPage + whiteSpace + 8, createGuiItem(Material.BOOK, ChatColor.GOLD + "Go to page " + (page + 1)));
        }

        if (page > 0) {
            inv.setItem(islandsOnPage + whiteSpace, createGuiItem(Material.BOOK, ChatColor.GOLD + "Go to page " + (page - 1)));
        }

        inv.setItem(islandsOnPage + whiteSpace + 4, createGuiItem(Material.CLOCK, ChatColor.GOLD + "Sort by " + parseSort(sort == 1 ? 0 : 1)));

        return inv;
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

    private int parsePage(String text) {
        Matcher matcher = Pattern.compile("\\d+").matcher(text);
        matcher.find();
        return Integer.parseInt(matcher.group());
    }

    private int parseSort(String text) {
        if (text.contains("age")) return 1;
        else return 0;
    }

    private String parseSort(int i) {
        if (i == 1) return "age";
        else return "name";
    }

    public VisitGui setPage(int page) {
        this.page = Math.max(page, 0);

        return this;
    }

    public VisitGui setSort(int sort) {
        this.sort = sort;

        return this;
    }

    public Inventory getDefaultInventory() {
        return setSort(1).setPage(0).getInventory();
    }
}
