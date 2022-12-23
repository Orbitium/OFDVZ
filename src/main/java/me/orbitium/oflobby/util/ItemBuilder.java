package me.orbitium.oflobby.util;

import me.orbitium.oflobby.OFDVZ;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    public static NamespacedKey key = new NamespacedKey(OFDVZ.getInstance(), "itemBuilder");
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(Material material, String name) {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
    }

    public ItemBuilder(Material material) {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder setMaterial(Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemBuilder setName(String newName) {
        if (!newName.isEmpty())
            itemMeta.setDisplayName(newName);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        itemMeta.setLore(lore);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        itemMeta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder addLore(String line) {
        List<String> lore = itemMeta.getLore();
        if (lore == null)
            lore = new ArrayList<>();
        lore.add(line);
        setLore(lore);
        return this;
    }

    public void setUnbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        itemStack.setItemMeta(itemMeta);
    }

    public void addEnchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
    }

    public void setAmount(int amount) {
        itemStack.setAmount(amount);
    }

    public ItemStack build() {
        return itemStack;
    }


    public ItemStack build(boolean buildMeta) {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}