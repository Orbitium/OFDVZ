package me.orbitium.oflobby;

import me.orbitium.oflobby.beacon.BeaconListener;
import me.orbitium.oflobby.classes.root.ClassManager;
import me.orbitium.oflobby.classes.root.ClassSelect;
import me.orbitium.oflobby.commands.*;
import me.orbitium.oflobby.event.Events;
import me.orbitium.oflobby.listener.BlockListener;
import me.orbitium.oflobby.listener.InventoryListener;
import me.orbitium.oflobby.listener.PlayerListener;
import me.orbitium.oflobby.lobby.Lobby;
import me.orbitium.oflobby.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class OFDVZ extends JavaPlugin {

    public static final ItemStack goldenNugget = new ItemStack(Material.GOLD_NUGGET);
    public static OFDVZ instance;
    public static Random random;
    public static InventoryHolder inventoryHolder = null;
    public static NamespacedKey zombieKey;
    public static NamespacedKey classKey;

    public static String deathTitle;
    public static String deathSubtitle;

    public static NamespacedKey compassData;
    public static Material emergencyBlock;

    @Override
    public void onEnable() {
        // Plugin startup logic
        random = new Random();
        saveDefaultConfig();
        deathTitle = ChatColor.translateAlternateColorCodes('&',getConfig().getString("messages.deathTitle"));
        deathSubtitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.deathSubTitle"));
        instance = this;

        zombieKey = new NamespacedKey(this, "isPlayerZombie");
        classKey = new NamespacedKey(this, "isClassSelected");
        compassData = new NamespacedKey(this, "compassData");
        emergencyBlock = Material.getMaterial(getConfig().getString("emergencyBlock"));

        ClassManager.loadClasses();
        ItemManager.loadItems();

        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        getCommand("class").setExecutor(new ClassCommandExecutor());
        getCommand("class").setTabCompleter(new ClassCommandCompleter());
        getCommand("game").setExecutor(new GameCommandExecutor());
        getCommand("game").setTabCompleter(new GameCommandCompleter());

        getCommand("event").setExecutor(new EventCommandExecutor());
        getCommand("event").setTabCompleter(new EventCommandTabCompleter());

        new ClassSelect();
        new Lobby();
        new Events();
        BeaconListener.initBeacon();
        OFScoreboard.init();
        CompassManager.initialize();
        SpawnerManager.init();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.getPersistentDataContainer().remove(classKey);
            onlinePlayer.getPersistentDataContainer().remove(zombieKey);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (BeaconListener.bossBar != null)
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            BeaconListener.bossBar.removePlayer(onlinePlayer);
        }
    }

    public static OFDVZ getInstance() {
        return instance;
    }
}
