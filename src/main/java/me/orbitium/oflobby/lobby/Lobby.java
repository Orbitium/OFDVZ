package me.orbitium.oflobby.lobby;

import me.libraryaddict.disguise.DisguiseAPI;
import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.OFScoreboard;
import me.orbitium.oflobby.beacon.BeaconListener;
import me.orbitium.oflobby.classes.root.ClassManager;
import me.orbitium.oflobby.classes.root.ClassSelect;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.event.Events;
import me.orbitium.oflobby.util.MessageUtil;
import me.orbitium.oflobby.util.ParticleUtil;
import me.orbitium.oflobby.util.StringParser;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Lobby {

    public static List<Player> waitingPlayers = new ArrayList<>();

    public static boolean playing = false;
    public static Location lobbyLocation;
    public static Location defenderStartLocation;
    public static Location attackerStartLocation;
    public static Location eventWaitForEventLocation;
    public static Location respawnSpawnLocation;

    //public static Map<Integer, String> announcements = new TreeMap<>(Collections.reverseOrder());
    public static int baseWait = -1;
    public static String baseMessage;

    public static int autoAssign;

    public Lobby() {
        lobbyLocation = loadLocation(OFDVZ.getInstance().getConfig().getString("lobby.location"));
        defenderStartLocation = loadLocation(OFDVZ.getInstance().getConfig().getString("game.defendersStartLocation"));
        attackerStartLocation = loadLocation(OFDVZ.getInstance().getConfig().getString("game.attackersStartLocation"));
        eventWaitForEventLocation = loadLocation(OFDVZ.getInstance().getConfig().getString("game.eventWaitLocation"));
        respawnSpawnLocation = loadLocation(OFDVZ.getInstance().getConfig().getString("game.deathPlayersRespawnWaitLocation"));

        baseWait = OFDVZ.getInstance().getConfig().getInt("lobby.countdownStart");
        baseMessage = OFDVZ.getInstance().getConfig().getString("lobby.countdownMessage");

        autoAssign = OFDVZ.getInstance().getConfig().getInt("ui.classSelectWait");
    }

    public static Location loadLocation(String string) {
        if (string.startsWith(" "))
            string = string.substring(1);
        String[] s = string.split(" ");

        World world = Bukkit.getWorld(s[0]);
        int x = Integer.parseInt(s[1]);
        int y = Integer.parseInt(s[2]);
        int z = Integer.parseInt(s[3]);

        return new Location(world, x, y, z);
    }

    public static void announce() {
        Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(StringParser.parse(baseMessage, new String[]{"[seconds]"}, 30 + ""));
            }
        }, (baseWait - 30) * 20);

        for (int i = 3; i > 0; i--) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                    onlinePlayer.sendMessage(StringParser.parse(baseMessage, new String[]{"[seconds]"}, (finalI) + ""));
            }, (baseWait - i) * 20);
        }

        Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), Lobby::start, baseWait * 20);

    }

    public static void prepare() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(lobbyLocation);
        }
        announce();
    }

    public static void start() {
        if (playing)
            return;
        playing = true;
        Lobby.attackerStartLocation.getWorld().setTime(9000);
        Lobby.attackerStartLocation.getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        Events.registerEvent();

        OFClass ofClass = ClassManager.getByName(OFDVZ.getInstance().getConfig().getString("ui.defenders.defaultClass"));
        if (ofClass == null)
            ofClass = ClassManager.builder;
        OFClass finalOfClass = ofClass;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(20);
            player.setSaturation(20);
            MessageUtil.sendMessageToPlayer(player, "messages.gameStarting");
            player.getPersistentDataContainer().remove(OFDVZ.classKey);
            player.getPersistentDataContainer().remove(OFDVZ.zombieKey);
            player.teleport(defenderStartLocation);
            ClassSelect.openUI(player);

            Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                if (!player.getPersistentDataContainer().has(OFDVZ.classKey, PersistentDataType.INTEGER)) {
                    finalOfClass.addPlayer(player, true);
                    MessageUtil.sendMessageToPlayer(player, "messages.yourClassAssignedAutomatically",
                            "[class_name]", finalOfClass.displayName);
                    player.closeInventory();
                }
            }, autoAssign * 20);
        }
        BeaconListener.q();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            MessageUtil.sendMessageToPlayer(onlinePlayer, "events.plague.announcementStart", "[second]",
                    Events.startDelay + "");
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            for (Entity entity : BeaconListener.center.getWorld().getEntities()) {
                if (entity instanceof Monster monster)
                    if (monster.getTarget() == null)
                        monster.setTarget(BeaconListener.center);
            }
        }, 30L, 30L);

    }

    public static void end() {
        Lobby.attackerStartLocation.getWorld().setTime(9000);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR)
                player.setGameMode(GameMode.SURVIVAL);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            DisguiseAPI.undisguiseToAll(player);
            player.teleport(lobbyLocation);
            player.getPersistentDataContainer().remove(OFDVZ.zombieKey);
            player.getPersistentDataContainer().remove(OFDVZ.classKey);
            for (ItemStack content : player.getInventory().getContents()) {
                if (content != null)
                    content.setAmount(0);
            }

            player.setPlayerListName(player.getName());
            BeaconListener.bossBar.removePlayer(player);
        }

        for (Team team : OFScoreboard.scoreboard.getTeams()) {
            for (String entry : team.getEntries()) {
                team.removeEntry(entry);
            }
        }

        Events.isPlagueStarted = false;
        playing = false;

        for (Map.Entry<ClassType, List<OFClass>> entry : ClassManager.classes.entrySet()) {
            for (OFClass ofClass : entry.getValue())
                ofClass.registeredPlayers.clear();
        }


        for (BukkitTask pendingTask : Bukkit.getScheduler().getPendingTasks()) {
            if (pendingTask.getOwner() == OFDVZ.getInstance())
                pendingTask.cancel();
        }
    }

    public static Set<Player> zp = new HashSet<>();

    public static void spawnParticle() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            for (Player onlinePlayer : zp) {
                ParticleUtil.spawnRedParticle(onlinePlayer, onlinePlayer.getLocation().add(-0.5, 2.1, -0.5));
                for (Player z : zp)
                    ParticleUtil.spawnRedParticle(onlinePlayer, z.getLocation().add(-0.5, 2.1, -0.5));
            }
        }, 5L, 5L);
    }
}
