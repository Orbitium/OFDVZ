package me.orbitium.oflobby;

import me.orbitium.oflobby.lobby.Lobby;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;

public class CompassManager {

    public static Map<Player, Integer> p = new HashMap<>();
    public static Map<String, String> d = new HashMap<>();
    public static Map<String, Location> stands = new HashMap<>();
    public static List<String> messages = new LinkedList<>();

    public static void initialize() {
        for (String s : OFDVZ.getInstance().getConfig().getStringList("compassTargetLocations")) {
            String[] ss = s.split(" ", 2);
            d.put(ss[0], ss[1]);
        }

        for (String s : OFDVZ.getInstance().getConfig().getStringList("compassTexts")) {
            try {
                String extracted = s.substring(s.indexOf('[') + 1, s.lastIndexOf(']'));
                if (extracted.length() >= 1) {

                    s = s.replace(extracted, d.get(extracted));
                    String l = d.get(extracted).replaceAll("\\[", "");
                    l = l.replaceAll("]", "");
                    Location location = Lobby.loadLocation(Lobby.lobbyLocation.getWorld().getName() + " " + l);
                    stands.put(s, location);
                    messages.add(s);
                }
            } catch (Exception ignored) {
                OFDVZ.getInstance().getLogger().log(Level.SEVERE, "Compass line error at message: " + s);
            }
        }
    }

    public static void t(Player player) {
        if (p.containsKey(player))
            remove(player);
        else
            add(player);
    }

    public static void add(Player player) {
        int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            display(player);
        }, 5L, 5L);

        p.put(player, task);
    }

    public static void remove(Player player) {
        int id = p.getOrDefault(player, -1);
        if (id != -1) {
            Bukkit.getScheduler().cancelTask(id);
            p.remove(player);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        }
    }

    public static void display(Player player) {
        int index = player.getPersistentDataContainer().get(OFDVZ.compassData, PersistentDataType.INTEGER);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                messages.get(index)
        ));
        player.setCompassTarget(stands.get(messages.get(index)));
    }

    public static void next(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int index = pdc.get(OFDVZ.compassData, PersistentDataType.INTEGER);
        if (index + 1 != messages.size())
            pdc.set(OFDVZ.compassData, PersistentDataType.INTEGER, index + 1);
        else
            pdc.set(OFDVZ.compassData, PersistentDataType.INTEGER, 0);
        display(player);
    }

}
