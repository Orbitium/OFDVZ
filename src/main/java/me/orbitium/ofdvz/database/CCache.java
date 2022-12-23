package me.orbitium.ofdvz.database;

import me.orbitium.ofdvz.classes.root.OFClass;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CCache {
    private static final Map<Class<? extends Event>, List<OFClass>> registeredClasses = new HashMap<>();

    public static void registerNewClass(Class<? extends Event> event, OFClass OFClass) {
        List<OFClass> list = registeredClasses.getOrDefault(event, new ArrayList<>());
        list.add(OFClass);
        registeredClasses.put(event, list);
    }

    public static void runClassOnEvent(Player player, Event event) {
        for (OFClass OFClass : registeredClasses.getOrDefault(event.getClass(), new ArrayList<>())) {
            if (OFClass.isPlayerRegistered(player))
                OFClass.tryRunEvent(player, event);
        }
    }
}
