package me.orbitium.ofdvz.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class ItemManager {

    public static ItemStack emptyItem;

    public static void loadItems() {
        emptyItem = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, " ").build();
    }

    public static List<Map<String, String>> readDataFromFile(String itemsFilePath) throws Exception {
        File dir = new File(itemsFilePath);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(itemsFilePath + "DummyItem.yml");
        if (!file.exists()) {
            file.createNewFile();
            createExampleFile(file);
            return null;
        }
        List<Map<String, String>> listOfMaps = new ArrayList<>();

        for (File file1 : dir.listFiles()) {
            Map<String, String> data = new HashMap<>();
            String line;
            String pLine;
            String label = "";
            Scanner scanner = new Scanner(file1);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().replaceAll("\t", "");
                pLine = line;
                if (!label.equals("itemLore:"))
                    line = line.replaceAll(" ", "");
                if (line.startsWith("#"))
                    continue;
                String[] s = line.split(":");
                if (s.length == 2) {
                    switch (s[0]) {
                        case "material" -> data.put("material", s[1]);
                        case "amount", "unbreakable" -> data.put(s[0], s[1]);
                        case "name" -> data.put("name", pLine.split(":")[1]);
                    }
                } else if (!line.startsWith("-")) {
                    label = line;
                } else {
                    line = line.substring(1);
                    if (label.equals("itemLore:"))
                        data.put("lore", data.getOrDefault("lore", "") + line + "\n");
                    else if (label.equals("enchants:"))
                        data.put("enchants", data.getOrDefault("enchants", "") + line + "\n");
                }
            }
            scanner.close();
            listOfMaps.add(data);
        }
        return listOfMaps;
    }

    public static List<ItemStack> generateItemsStacks(List<Map<String, String>> dataList) {
        List<ItemStack> itemStacks = new ArrayList<>();

        for (Map<String, String> data : dataList) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.DIRT);
            Material material = Material.getMaterial(data.getOrDefault("material", "STONE").replaceAll(" ", ""));
            int amount = Integer.parseInt(data.getOrDefault("amount", "1"));
            String name = data.getOrDefault("name", "");
            List<String> lore = new ArrayList<>();
            boolean unbreakable = Boolean.parseBoolean(data.getOrDefault("unbreakable", "false"));

            for (String s : data.getOrDefault("lore", "").split("\n")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', s));
            }

            itemBuilder.setMaterial(material);
            itemBuilder.setAmount(amount);
            itemBuilder.setName(ChatColor.translateAlternateColorCodes('&', name));
            if (!lore.isEmpty())
                itemBuilder.setLore(lore);
            itemBuilder.setUnbreakable(unbreakable);

            String[] enchantLines = data.getOrDefault("enchants", "").split("\n");
            for (String line : enchantLines) {
                if (line.isEmpty())
                    continue;
                int amplifier = Integer.parseInt(String.valueOf(line.toCharArray()[line.length() - 1]));
                String[] s = line.split(" ");
                String a = "";
                if (s[0].length() > 2)
                    a = s[0];
                if (a.isEmpty())
                    a = s[1];
                a = a.replaceAll("\n", "");
                a = a.substring(0, a.length() - 1);
                Enchantment enchantment = Enchantment.getByKey(new NamespacedKey(NamespacedKey.MINECRAFT, a.toLowerCase(Locale.US)));
                if (enchantment != null) {
                    itemBuilder.addEnchant(enchantment, amplifier);
                }
            }
            ItemStack itemStack = itemBuilder.build();
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.getPersistentDataContainer().set(ItemBuilder.key, PersistentDataType.INTEGER, 0);
            itemStack.setItemMeta(itemMeta);
            itemStacks.add(itemStack);
        }
        return itemStacks;
    }

    public static void createExampleFile(File file) throws Exception {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("# The item's type, like iron shovel\n");
        fileWriter.write("material: IRON_SHOVEL\n\n");

        fileWriter.write("# The amount of item\n");
        fileWriter.write("amount: 1\n\n");

        fileWriter.write("# The name can be empty\n");
        fileWriter.write("name: DummyName\n\n");

        fileWriter.write("# The lore of item\n");
        fileWriter.write("# Don't forget to put `-` at the start of the line\n");
        fileWriter.write("itemLore:\n");
        fileWriter.write("\t-Line 1\n");
        fileWriter.write("\t-Line 2\n");
        fileWriter.write("\t-Line 3\n\n");

        fileWriter.write("# The item will have durability. If it's true, the item will never break\n");
        fileWriter.write("unbreakable: true\n\n");

        fileWriter.write("# Enchants of the item\n");
        fileWriter.write("# Don't forget put `-` at the start of the line\n");
        fileWriter.write("# The format is {Effect_Name} {amplifiers}\n");
        fileWriter.write("enchants:\n");
        fileWriter.write("\t- EFFICIENCY 1\n");
        fileWriter.write("\t- PUNCH 2");

        fileWriter.close();
    }

    public static boolean convertItem(Player player, int convertAmount, ItemStack newItem, UsageLimiter limiter, String itemName, String resourceName) {
        if (!limiter.check(player))
            return false;

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        int amount = itemStack.getAmount();
        if (amount >= convertAmount) {
            player.getInventory().getItemInMainHand().setAmount(amount - convertAmount);
            player.getInventory().addItem(newItem);
            limiter.update(player);
            MessageUtil.sendMessageToPlayer(player, "messages.itemCrafted", "[item_name]", itemName);
            return true;
        }
        MessageUtil.sendMessageToPlayer(player, "messages.error.notEnoughResource", "[source_name]", resourceName);

        return false;
    }
    /* File Format
        # The item's type, like iron shovel
        material: IRON_SHOVEL

        # The amount of item
        amount: 1

        # The name can be empty
        name: DummyName

        # The lore of item
        # Don't forget to put `-` at the start of the line
        itemLore:
            - Line 1
            - Line 2
            - ...

        # The item will have durability. If it's true, the item will never break
        unbreakable: true

        # Enchants of the item
        # Don't forget put `-` at the start of the line
        # The format is {Effect_Name} {amplifiers}
        enchants:
            - EFFICIENCY 1
            - PUNCH 2
     */
}