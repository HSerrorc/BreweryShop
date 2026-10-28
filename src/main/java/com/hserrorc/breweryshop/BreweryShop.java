package com.hserrorc.breweryshop;

import com.dre.brewery.api.BreweryApi;
import com.dre.brewery.recipe.BRecipe;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreweryShop extends JavaPlugin {

    private Economy economy;
    private Map<String, Double> prices = new HashMap<>();
    private GuiLayout guiLayout;
    private boolean debug;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("未找到 Vault 经济系统，插件已禁用！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("BreweryX") == null) {
            getLogger().severe("未找到 BreweryX 插件，插件已禁用！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadConfig();

        getCommand("sellbrew").setExecutor(new SellCommand(this));
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("BreweryShop 已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("BreweryShop 已禁用！");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        debug = config.getBoolean("debug", false);

        prices.clear();
        if (config.contains("prices")) {
            for (String key : config.getConfigurationSection("prices").getKeys(false)) {
                prices.put(key, config.getDouble("prices." + key));
            }
        }

        List<String> layout = config.getStringList("gui.layout");
        String title = config.getString("gui.title", "酒类出售");
        guiLayout = new GuiLayout(title, layout);
    }

    public Economy getEconomy() {
        return economy;
    }

    public double getPrice(String recipeId, int quality) {
        String key = recipeId + "-" + quality;
        return prices.getOrDefault(key, 0.0);
    }

    public GuiLayout getGuiLayout() {
        return guiLayout;
    }

    public Inventory createShopGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, guiLayout.getSize(), guiLayout.getTitle());

        for (int i = 0; i < guiLayout.getSize(); i++) {
            char slot = guiLayout.getSlot(i);
            if (slot == '=') {
                ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                inv.setItem(i, border);
            } else if (slot == 'S') {
                updateSellButton(inv, i, 0);
            }
        }

        return inv;
    }

    public void updateSellButton(Inventory inv, int slot, double totalPrice) {
        ItemStack sellButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        var meta = sellButton.getItemMeta();
        meta.setDisplayName("§a§l确认出售");
        meta.setLore(List.of(
            "",
            "§e本次出售: §6" + String.format("%.2f", totalPrice) + " §e金币",
            "",
            "§8▸ §7左键确认出售"
        ));
        sellButton.setItemMeta(meta);
        inv.setItem(slot, sellButton);
    }

    public double calculateTotal(Inventory inv) {
        double total = 0.0;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() != Material.POTION) continue;

            debug("发现药水物品: " + item.getType());

            if (!BreweryApi.isBrew(item)) {
                debug("  不是 BreweryX 酒品");
                continue;
            }

            var brew = BreweryApi.getBrew(item);
            if (brew == null) {
                debug("  Brew 对象为空");
                continue;
            }

            if (!brew.isSealed()) {
                debug("  酒品未封存");
                continue;
            }

            BRecipe recipe = brew.getCurrentRecipe();
            if (recipe == null) {
                debug("  配方为空");
                continue;
            }

            int quality = brew.getQuality();
            String recipeId = recipe.getId();
            String priceKey = recipeId + "-" + quality;
            double price = getPrice(recipeId, quality);

            debug("  配方ID: " + recipeId);
            debug("  品质: " + quality);
            debug("  价格键: " + priceKey);
            debug("  单价: " + price);

            total += price * item.getAmount();
        }

        debug("总价: " + total);
        return total;
    }

    private void debug(String message) {
        if (debug) {
            getLogger().info("[DEBUG] " + message);
        }
    }
}
