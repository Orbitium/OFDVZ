package me.orbitium.oflobby.listener;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.classes.root.ClassSelect;
import me.orbitium.oflobby.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING) {
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack != null && itemStack.hasItemMeta()) {
                PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
                if (pdc.has(ItemBuilder.key, PersistentDataType.INTEGER)) {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() != OFDVZ.inventoryHolder)
            return;

        if (ClassSelect.title.containsValue(event.getView().getTitle()) && event.getCurrentItem() != null) {
            ClassSelect.selectClass((Player) event.getWhoClicked(), event.getCurrentItem().getType());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() != OFDVZ.inventoryHolder)
            return;

        if (ClassSelect.title.containsValue(event.getView().getTitle())) {
            PersistentDataContainer pdc = event.getPlayer().getPersistentDataContainer();

            if (pdc.has(OFDVZ.classKey, PersistentDataType.INTEGER))
                return;

            if (!event.getPlayer().getPersistentDataContainer().has(ClassSelect.refresh, PersistentDataType.INTEGER))
                Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                    if (pdc.has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
                        ClassSelect.openZombieUI((Player) event.getPlayer());
                    else
                        ClassSelect.openUI((Player) event.getPlayer());
                }, 1L);

        }
    }

}
