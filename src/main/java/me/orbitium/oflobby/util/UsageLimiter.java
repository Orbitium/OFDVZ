package me.orbitium.oflobby.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class UsageLimiter {

    private Map<Player, Integer> counter = new HashMap<>();
    int cooldown;

    public UsageLimiter(int cooldown) {
        this.cooldown = cooldown;
    }

    public boolean check(Player player) {
        boolean b = counter.getOrDefault(player, 0) + cooldown <= Time.dateToInt();
        if (!b)
            MessageUtil.sendMessageToPlayer(player, "messages.error.cooldownIsNotFinished", "[remain]",
                    getRemain(player) + "");
        return b;
    }

    public int getRemain(Player player) {
        return (cooldown + counter.get(player)) - Time.dateToInt();
    }

    public void update(Player player) {
        counter.put(player, Time.dateToInt());
    }
}
