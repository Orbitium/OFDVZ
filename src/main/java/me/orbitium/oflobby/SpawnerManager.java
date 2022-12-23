package me.orbitium.oflobby;

import me.orbitium.oflobby.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SpawnerManager {

    public static List<CreatureSpawner> spawners = new ArrayList<>();
    public static int spawnAmount;
    public static int increaseAmount;
    public static int timeSpace;
    public static int maxSpawn;

    public static void init() {
        List<String> data = OFDVZ.getInstance().getConfig().getStringList("spawner.spawnerLocations");
        for (String d : data) {
            Location location = Lobby.loadLocation(d);
            if (location.getBlock().getState() instanceof CreatureSpawner spawner) {
                spawners.add(spawner);
                spawner.setRequiredPlayerRange(OFDVZ.getInstance().getConfig().getInt("spawner.spawnerRange"));
                spawner.update(true, true);
            } else
                OFDVZ.getInstance().getLogger().log(Level.SEVERE, "Spawner isn't found at location: " +
                        location.getWorld() + " " + location.getX() + " " + location.getY() + " " + location.getZ());
        }

        spawnAmount = OFDVZ.getInstance().getConfig().getInt("spawner.spawnAmountPerRun");
        increaseAmount = OFDVZ.getInstance().getConfig().getInt("spawner.spawnAmountIncrease");
        timeSpace = OFDVZ.getInstance().getConfig().getInt("spawner.spawnAmountIncreasePer");
        maxSpawn = OFDVZ.getInstance().getConfig().getInt("spawner.maxSpawnAmount");
    }

    public static void start() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            for (CreatureSpawner spawner : spawners) {
                spawner.setSpawnCount(Math.min(spawner.getSpawnCount() + increaseAmount, maxSpawn));
                spawner.setMaxNearbyEntities(spawner.getSpawnCount() * 2);
                spawner.setMinSpawnDelay(Math.max(40, spawner.getMinSpawnDelay() - 1));
                spawner.setMaxSpawnDelay(Math.max(80, spawner.getMaxSpawnDelay() - 1));
                spawner.update(true,true);
            }
        }, timeSpace * 20, timeSpace * 20);
    }
}
