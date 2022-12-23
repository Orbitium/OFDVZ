package me.orbitium.ofdvz.util;

import me.orbitium.ofdvz.OFDVZ;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class InventoryUtil {

    public static Inventory createEmptyInventory(String title, int size) {
        Inventory inventory = Bukkit.createInventory(OFDVZ.inventoryHolder, size, title);
        for (int i = 0; i < size; i++)
            inventory.setItem(i, ItemManager.emptyItem);
        return inventory;
    }

}
