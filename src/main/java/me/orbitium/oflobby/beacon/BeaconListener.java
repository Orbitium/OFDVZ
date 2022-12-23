package me.orbitium.oflobby.beacon;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.lobby.Lobby;
import me.orbitium.oflobby.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

public class BeaconListener implements Listener {

    static double currentHealth;
    static double maxHealth;
    static Beacon beacon;
    public static ArmorStand center;
    public static Location beaconLocation;
    public static int beaconRange;
    public static BossBar bossBar;

    public static void initBeacon() {
        beaconLocation = Lobby.loadLocation(OFDVZ.getInstance().getConfig().getString("beacon.beaconLocation"));
        beaconRange = OFDVZ.getInstance().getConfig().getInt("beacon.protectionRange");
        maxHealth = currentHealth = (double) OFDVZ.getInstance().getConfig().getInt("beacon.maxBeaconHealth");
        Block block = beaconLocation.getWorld().getBlockAt(beaconLocation);
        if (block.getType() != Material.BEACON) {
            for (int i = 0; i < 6; i++) {
                OFDVZ.getInstance().getLogger().log(Level.SEVERE, "Beacon cannot found in: " +
                        OFDVZ.getInstance().getConfig().getString("beacon.beaconLocation"));
            }
            OFDVZ.getInstance().getPluginLoader().disablePlugin(OFDVZ.getInstance());
            return;
        }

        beacon = (Beacon) block.getState();
        for (Entity e : beaconLocation.getWorld().getNearbyEntities(beaconLocation, 5, 5, 5)) {
            if (e.getType() == EntityType.ARMOR_STAND) {
                center = (ArmorStand) e;
                break;
            }
        }
        if (center == null)
            center = (ArmorStand) beaconLocation.getWorld().spawnEntity(beaconLocation.add(0.5, 1, 0.5), EntityType.ARMOR_STAND);

        center.setVisible(false);
        center.setInvulnerable(true);
        center.setGravity(false);
    }

    public static void q() {
        String title = OFDVZ.getInstance().getConfig().getString("beacon.bossBarTitle");
        BarColor barColor = BarColor.valueOf(OFDVZ.getInstance().getConfig().getString("beacon.bossBarColor"));
        NamespacedKey barKey = new NamespacedKey(OFDVZ.getInstance(), "beaconHealth");
        bossBar = Bukkit.createBossBar(barKey, title, barColor, BarStyle.SOLID);
        bossBar.setProgress(currentHealth / maxHealth);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Bukkit.getBossBars().forEachRemaining(b -> {
                b.removePlayer(onlinePlayer);
            });
            bossBar.addPlayer(onlinePlayer);
        }

        startCheck();
    }


    static int taskID;
    static int taskID2;

    public static void startCheck() {
        final int[] timeWaiter = {20};
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            for (Entity nearbyEntity : center.getNearbyEntities(beaconRange, beaconRange, beaconRange)) {
                if (nearbyEntity.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER)
                        || nearbyEntity instanceof Monster) {
                    if (timeWaiter[0] == 20) {
                        timeWaiter[0] = 0;
                        currentHealth--;
                        bossBar.setProgress(Math.max(0, currentHealth) / maxHealth);
                        if (0.0 >= currentHealth)
                            finish();
                        else
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                                MessageUtil.sendActionBarMessage(onlinePlayer, "messages.beaconTakingDamage");
                    } else
                        timeWaiter[0] += 10;
                }
                break;

            }
        }, 10L, 10L);


        int healthDelay = OFDVZ.getInstance().getConfig().getInt("beacon.beaconRegenCooldown");
        taskID2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            for (Entity nearbyEntity : center.getNearbyEntities(beaconRange, beaconRange, beaconRange)) {
                if (nearbyEntity.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER)
                        || nearbyEntity instanceof Monster)
                    return;
            }
            currentHealth = Math.min(currentHealth + 1, maxHealth);
            bossBar.setProgress(currentHealth / maxHealth);
        }, healthDelay * 20, healthDelay * 20);
    }

    public static void finish() {
        Bukkit.getScheduler().cancelTask(taskID);
        for (Player player : Bukkit.getOnlinePlayers()) {
            MessageUtil.sendMessageToPlayer(player, "messages.beaconIsLostTitle");
        }
        Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), Lobby::end, 15 * 20);
        Bukkit.getScheduler().cancelTask(taskID);
        Bukkit.getScheduler().cancelTask(taskID2);
    }

}
