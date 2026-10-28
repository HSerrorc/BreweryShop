package com.hserrorc.breweryshop;

import com.dre.brewery.api.BreweryApi;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    private final BreweryShop plugin;

    public GuiListener(BreweryShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(plugin.getGuiLayout().getTitle())) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();

        if (slot < 0 || slot >= inv.getSize()) {
            scheduleUpdate(player, inv);
            return;
        }

        char slotType = plugin.getGuiLayout().getSlot(slot);

        if (slotType == '=') {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 0.3f, 0.8f);
            return;
        }

        if (slotType == 'S') {
            event.setCancelled(true);
            handleSell(player, inv);
            return;
        }

        scheduleUpdate(player, inv);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(plugin.getGuiLayout().getTitle())) return;
        if (event.getRawSlots().stream().noneMatch(slot -> slot < event.getInventory().getSize())) return;
        scheduleUpdate((Player) event.getWhoClicked(), event.getInventory());
    }

    private void scheduleUpdate(Player player, Inventory inv) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().equals(inv)) {
                updateGui(inv);
            }
        }, 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(plugin.getGuiLayout().getTitle())) return;

        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();

        for (int i = 0; i < inv.getSize(); i++) {
            char slotType = plugin.getGuiLayout().getSlot(i);
            if (slotType == ' ') {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    player.getInventory().addItem(item);
                }
            }
        }
    }

    private void updateGui(Inventory inv) {
        double total = plugin.calculateTotal(inv);
        int sellSlot = plugin.getGuiLayout().getSellButtonSlot();
        if (sellSlot >= 0) {
            plugin.updateSellButton(inv, sellSlot, total);
        }
    }

    private void handleSell(Player player, Inventory inv) {
        double total = plugin.calculateTotal(inv);

        if (total <= 0) {
            player.sendMessage("§c没有可出售的酒品！");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int itemsSold = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            char slotType = plugin.getGuiLayout().getSlot(i);
            if (slotType == ' ') {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() == Material.POTION) {
                    if (BreweryApi.isBrew(item)) {
                        var brew = BreweryApi.getBrew(item);
                        if (brew != null && brew.isSealed()) {
                            itemsSold += item.getAmount();
                            inv.setItem(i, null);
                        }
                    }
                }
            }
        }

        plugin.getEconomy().depositPlayer(player, total);
        player.sendMessage(String.format("§a§l✓ §a出售成功！出售 §e%d §a瓶酒品，获得 §6%.2f §a金币", itemsSold, total));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
        player.closeInventory();
    }
}
