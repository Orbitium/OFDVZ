package me.orbitium.ofdvz.event;

import me.orbitium.ofdvz.OFDVZ;
import me.orbitium.ofdvz.SpawnerManager;
import me.orbitium.ofdvz.classes.root.ClassManager;
import me.orbitium.ofdvz.classes.root.OFClass;
import me.orbitium.ofdvz.lobby.Lobby;
import me.orbitium.ofdvz.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Events {

    public static boolean isPlagueStarted = false;
    private static BukkitTask task;
    public static int startDelay;

    public Events() {
        startDelay = OFDVZ.getInstance().getConfig().getInt("events.plague.startAfter");
    }

    public static void registerEvent() {
        task = Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
            if (!isPlagueStarted)
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    MessageUtil.sendMessageToPlayer(onlinePlayer, "events.plague.annocumentStartText");
                }
            startStartPlague(OFDVZ.getInstance().getConfig().getInt("events.plague.convertPercentage"));
        }, startDelay * 20);

        Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
            if (!isPlagueStarted)
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    MessageUtil.sendMessageToPlayer(onlinePlayer, "events.plague.announcementStart", "[second]",
                            30 + "");
                }
            doNight();
        }, (startDelay - 30) * 20);

        for (int i = 3; i > 0; i--) {
            int finalI = i;
            if (!isPlagueStarted)
                Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        MessageUtil.sendMessageToPlayer(onlinePlayer, "events.plague.announcementStart", "[second]",
                                finalI + "");
                    }
                }, (startDelay - i) * 20);
        }

    }

    public static void doNight() {
        for (int i = 1; i < 5000; i++) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                Lobby.attackerStartLocation.getWorld().setTime((9000 + finalI));
            }, 1 + (i / 9));
        }
    }

    public static void startStartPlague(int amount) {
        if (isPlagueStarted)
            return;
        isPlagueStarted = true;
        if (task != null)
            task.cancel();

        int allPlayers = Bukkit.getOnlinePlayers().size();
        int targetPlayers = Math.max(1, ((allPlayers * amount) / 100) - Lobby.waitingPlayers.size());
        targetPlayers = Math.min(Bukkit.getOnlinePlayers().size(), targetPlayers);

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.removeAll(Lobby.waitingPlayers);

        int requestAmount = Math.min(targetPlayers, allPlayers);

        List<Player> zombies = new ArrayList<>(Lobby.waitingPlayers);

        while (zombies.size() < requestAmount) {
            for (Player player : players) {
                if (OFDVZ.random.nextBoolean())
                    if (zombies.size() + 1 <= requestAmount)
                        zombies.add(player);
                    else
                        break;
            }
            players.removeAll(zombies);
        }

        OFClass ofClass = ClassManager.getByName(OFDVZ.getInstance().getConfig().getString("ui.attackers.defaultClass"));
        if (ofClass == null)
            ofClass = ClassManager.zombie;
        OFClass finalOfClass = ofClass;

        for (Player player : zombies) {
            Events.becomeZombie(player);
            player.getPersistentDataContainer().set(OFDVZ.classKey, PersistentDataType.INTEGER, 0);
            Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                if (!player.getPersistentDataContainer().has(OFDVZ.classKey, PersistentDataType.INTEGER)) {
                    finalOfClass.addPlayer(player, true);
                    MessageUtil.sendMessageToPlayer(player, "messages.yourClassAssignedAutomatically",
                            "[class_name]", finalOfClass.displayName);
                    player.closeInventory();
                }
            }, Lobby.autoAssign * 20);
        }
        Lobby.spawnParticle();
        SpawnerManager.start();
    }

    public static void becomeZombie(Player player) {
        try {
            MessageUtil.sendMessageToPlayer(player, "events.plague.becomeZombieMessage");
            OFClass ofClass = ClassManager.getClassFromPlayer(player);
            ofClass.die(player);
        } catch (Exception exception) {
            ClassManager.alchemist.die(player);
        }
    }

}
