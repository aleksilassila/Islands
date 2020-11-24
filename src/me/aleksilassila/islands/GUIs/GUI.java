package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GUI {
    public abstract ChestGui getMainGui();
    public abstract Player getPlayer();

    public void open() {
        getMainGui().show(getPlayer());
    }

    protected ItemStack createGuiItem(final Material material, final String name, boolean shiny, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        if (shiny) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setDisplayName(name);

        List<String> lores = new ArrayList<>();

        for (String l : lore) {
            lores.addAll(Arrays.asList(l.split("\n")));
        }

        meta.setLore(lores);
        item.setItemMeta(meta);

        return item;
    }

    protected void addBackground(ChestGui gui, Material material) {
        OutlinePane background = new OutlinePane(0, 0, 9, gui.getRows(), Pane.Priority.LOWEST);
        background.addItem(new GuiItem(createGuiItem(material, "" + ChatColor.RESET, false)));
        background.setRepeat(true);

        gui.addPane(background);
    }
}
