package me.orbitium.oflobby.classes.root;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.lobby.Lobby;
import me.orbitium.oflobby.util.InventoryUtil;
import me.orbitium.oflobby.util.ItemBuilder;
import me.orbitium.oflobby.util.MessageUtil;
import me.orbitium.oflobby.util.StringParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSelect {

    private static Configuration config;
    public static Map<ClassType, List<Player>> playerList = new HashMap<>();
    public static Map<ClassType, String> title = new HashMap<>();
    private static String playerCountDisplayEmpty;
    private static String playerCountDisplayFull;
    private static String locked;
    private static String unlocked;
    public static NamespacedKey refresh;

    public ClassSelect() {
        config = OFDVZ.getInstance().getConfig();
        refresh = new NamespacedKey(OFDVZ.getInstance(), "refresh");

        title.put(ClassType.Defender, ChatColor.translateAlternateColorCodes('&', config.getString("ui.defenders.classSelectTitle")));
        title.put(ClassType.Zombies, ChatColor.translateAlternateColorCodes('&', config.getString("ui.attackers.classSelectTitle")));
        playerCountDisplayEmpty = config.getString("ui.playerCountDisplayEmpty");
        playerCountDisplayFull = config.getString("ui.playerCountDisplayFull");
        locked = config.getString("ui.attackers.lockedLore");
        unlocked = config.getString("ui.attackers.unlockedLore");
    }

    public static void openUI(Player player) {
        if (player.getPersistentDataContainer().has(OFDVZ.classKey, PersistentDataType.INTEGER))
            return;
        List<Player> l = playerList.getOrDefault(ClassType.Defender, new ArrayList<>());
        if (!l.contains(player))
            l.add(player);
        playerList.put(ClassType.Defender, l);

        List<OFClass> ct = ClassManager.classes.get(ClassType.Defender);

        Inventory inventory = InventoryUtil.createEmptyInventory(title.get(ClassType.Defender), 27);

        for (OFClass defender : ct) {
            ItemBuilder itemBuilder = new ItemBuilder(defender.getDisplayItem());
            int currentPlayerAmount = defender.getRegisteredPlayers().size();

            itemBuilder.setLore(defender.lore);

            if (currentPlayerAmount < defender.getMaxPlayer())
                itemBuilder.addLore(StringParser.parse(playerCountDisplayEmpty, new String[]{"[current_player]", "[max_player]"},
                        defender.getRegisteredPlayers().size() + "", defender.getMaxPlayer() + ""));
            else
                itemBuilder.addLore(StringParser.parse(playerCountDisplayFull, new String[]{"[current_player]", "[max_player]"},
                        defender.getRegisteredPlayers().size() + "", defender.getMaxPlayer() + ""));

            itemBuilder.setName(ChatColor.translateAlternateColorCodes('&', defender.displayName));

            inventory.setItem(defender.getDisplaySlot(), itemBuilder.build(true));
        }

        player.openInventory(inventory);
    }

    public static void openZombieUI(Player player) {
        double percentage = ((double) Lobby.zp.size() / (double) Bukkit.getOnlinePlayers().size()) * 100;
        Inventory inventory = InventoryUtil.createEmptyInventory(title.get(ClassType.Zombies), 27);

        for (OFClass defender : ClassManager.classes.get(ClassType.Zombies)) {
            ItemBuilder itemBuilder = new ItemBuilder(defender.getDisplayItem());
            int currentPlayerAmount = defender.getRegisteredPlayers().size();

            itemBuilder.setLore(defender.lore);

            if (currentPlayerAmount < defender.getMaxPlayer())
                itemBuilder.addLore(StringParser.parse(playerCountDisplayEmpty, new String[]{"[current_player]", "[max_player]"},
                        defender.getRegisteredPlayers().size() + "", defender.getMaxPlayer() + ""));
            else
                itemBuilder.addLore(StringParser.parse(playerCountDisplayFull, new String[]{"[current_player]", "[max_player]"},
                        defender.getRegisteredPlayers().size() + "", defender.getMaxPlayer() + ""));

            if (percentage >= defender.unlockPercentage)
                itemBuilder.addLore(StringParser.parse(unlocked, new String[]{"[percentage]"},
                        defender.unlockPercentage + ""));
            else
                itemBuilder.addLore(StringParser.parse(locked, new String[]{"[percentage]"},
                        defender.unlockPercentage + ""));

            itemBuilder.setName(ChatColor.translateAlternateColorCodes('&', defender.displayName));

            inventory.setItem(defender.getDisplaySlot(), itemBuilder.build(true));
        }
        List<Player> l = playerList.getOrDefault(ClassType.Defender, new ArrayList<>());
        if (!l.contains(player))
            l.add(player);
        playerList.put(ClassType.Defender, l);
        player.openInventory(inventory);
    }

    public static void closeUI(Player player) {
        playerList.remove(player);
    }

    public static void updateUI(ClassType classType) {
        List<Player> list = new ArrayList<>(playerList.get(classType));
        for (Player player : list) {
            player.getPersistentDataContainer().set(refresh, PersistentDataType.INTEGER, 0);
            openUI(player);
            player.getPersistentDataContainer().remove(refresh);
        }
    }

    public static void selectClass(Player player, Material material) {
        double percentage = ((double) Lobby.zp.size() / (double) Bukkit.getOnlinePlayers().size()) * 100;
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        List<OFClass> targetGroup;

        boolean b = false;

        if (pdc.has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
            targetGroup = ClassManager.classes.get(ClassType.Zombies);
        else
            targetGroup = ClassManager.classes.get(ClassType.Defender);

        for (OFClass targetClass : targetGroup) {
            if (targetClass.displayItem.getType().equals(material)) {

                if (targetClass.unlockPercentage > percentage) {
                    MessageUtil.sendMessageToPlayer(player, "messages.error.classIsLocked");
                    return;
                }

                if (targetClass.addPlayer(player, false)) {
                    player.closeInventory();
                    MessageUtil.sendMessageToPlayer(player, "messages.classSelected", "[class_name]", targetClass.name);
                    b = true;
                    break;
                } else {
                    MessageUtil.sendMessageToPlayer(player, "messages.error." +
                            "classGroupIsFull");
                    return;
                }
            }
        }


        if (!b)
            return;

        if (pdc.has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
            playerList.getOrDefault(ClassType.Zombies, new ArrayList<>()).remove(player);
        else
            playerList.getOrDefault(ClassType.Defender, new ArrayList<>()).remove(player);
        player.getPersistentDataContainer().set(OFDVZ.classKey, PersistentDataType.INTEGER, 0);

        updateUI(ClassType.Defender);
    }

}
