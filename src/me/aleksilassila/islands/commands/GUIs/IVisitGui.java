package me.aleksilassila.islands.commands.GUIs;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public interface IVisitGui extends InventoryHolder {
    public void onInventoryClick(Player whoClicked, int slot, ItemStack clickedItem, InventoryView inventoryView);
}
