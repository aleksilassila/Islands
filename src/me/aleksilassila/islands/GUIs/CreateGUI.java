package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.BiomeMaterials;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CreateGUI extends PageGUI {
    private final Islands plugin;
    private final Player player;

    private final String subcommand;
    private final double recreateCost;
    private Double oldCost = null;

    private final int PAGE_HEIGHT = 4; // < 1

    public CreateGUI(Islands plugin, Player player, String subcommand) {
        this.plugin = plugin;
        this.player = player;
        this.subcommand = subcommand;

        recreateCost = subcommand.equalsIgnoreCase("recreate") ? plugin.getConfig().getDouble("economy.recreateCost") : 0;
    }

    public CreateGUI setOldCost(double cost) {
        oldCost = cost;

        return this;
    }

    @Override
    public Gui getMainGui() {
        return createPaginatedGUI(PAGE_HEIGHT, Messages.get("gui.create.TITLE"), availableIslandPanes());
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    private List<StaticPane> availableIslandPanes() {
        List<StaticPane> panes = new ArrayList<>();

        HashMap<Biome, List<Location>> availableLocations = plugin.islandGeneration.biomes.availableLocations;

        List<Biome> sortedSet = new ArrayList<>(availableLocations.keySet());

        sortedSet.sort(Comparator.comparingDouble(a -> BiomeMaterials.valueOf(a.name()).getMaterial().name().charAt(0) + 1 / (double) BiomeMaterials.valueOf(a.name()).getMaterial().name().charAt(1)));

        StaticPane pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);

        int itemCount = 0;
        for (Biome biome : sortedSet) {
            if (itemCount == 0 && !plugin.getConfig().getBoolean("disableRandomBiome")) {
                pane.addItem(new GuiItem(
                        createGuiItem(
                                Material.COMPASS,
                                Messages.get("gui.create.BIOME_NAME", "RANDOM"),
                                true,
                                Messages.get("gui.create.BIOME_LORE", 0)
                        ),
                        inventoryClickEvent -> {
                            getSizeGui(null).show(inventoryClickEvent.getWhoClicked());
                        }), 0, 0);

                itemCount++;
            }

            if (pane.getItems().size() >= (PAGE_HEIGHT - 1) * 9) {
                panes.add(pane);
                pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);
            }

            pane.addItem(new GuiItem(
                    createGuiItem(
                            BiomeMaterials.valueOf(biome.name()).getMaterial(),
                            Messages.get("gui.create.BIOME_NAME", biome.name()),
                            false,
                            Messages.get("gui.create.BIOME_LORE", availableLocations.get(biome).size())
                    ),
                    event -> {
                        getSizeGui(biome).show(event.getWhoClicked());
                    }), (itemCount % (9 * (PAGE_HEIGHT - 1))) % 9, (itemCount % (9 * (PAGE_HEIGHT - 1))) / 9);
            itemCount++;
        }

        if (pane.getItems().size() > 0) panes.add(pane);

        return panes;
    }

    private Gui getSizeGui(Biome biome) {
        Gui gui = createPaginatedGUI(2, Messages.get("gui.create.SIZE_TITLE"), availableSizePanes("island " + subcommand + " " + (biome == null ? "RANDOM" : biome.name())));
        gui.setOnTopClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

        if (plugin.econ != null) {
            StaticPane balance = new StaticPane(0, 1, 1, 1);

            balance.addItem(new GuiItem(createGuiItem(Material.EMERALD, Messages.get("gui.create.BALANCE"), true, Messages.get("gui.create.BALANCE_LORE", plugin.econ.getBalance(player)))), 0, 0);

            gui.addPane(balance);
        }

        return addMainMenuButton(gui);
    }

    private List<StaticPane> availableSizePanes(String createCommand) {
        List<StaticPane> panes = new ArrayList<>();

        StaticPane pane = new StaticPane(0, 0, 9, 1);

        int itemCount = 0;
        for (String key : plugin.definedIslandSizes.keySet()) {
            if (pane.getItems().size() >= 9) {
                panes.add(pane);
                pane = new StaticPane(0, 0, 9, 1);
            }

            int islandSize = plugin.definedIslandSizes.get(key);
            if (!player.hasPermission(plugin.getCreatePermission(islandSize))) continue;

            double cost = 0.0;

            if (plugin.econ != null) {
                cost = plugin.islandPrices.getOrDefault(islandSize, 0.0) + recreateCost;

                if (oldCost != null) {
                    cost = Math.max(cost - oldCost, 0);
                }
                
                if (player.hasPermission(Permissions.bypass.economy)) cost = 0;
            }


            pane.addItem(
                new GuiItem(
                    createGuiItem(
                        Material.BOOK,
                        Messages.get("gui.create.SIZE_NAME", key), false,
                        Messages.get("gui.create.SIZE_LORE", islandSize, cost)
                    ),
                    event -> {
                        event.getWhoClicked().closeInventory();
                        ((Player) event.getWhoClicked()).performCommand(createCommand + " " + key);
                    }
                ), itemCount % 9, itemCount / 9);

            itemCount++;
        }

        if (pane.getItems().size() > 0) panes.add(pane);

        return panes;
    }
}
