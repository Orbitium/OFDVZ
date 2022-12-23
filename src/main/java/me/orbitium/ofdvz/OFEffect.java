package me.orbitium.ofdvz;

import me.orbitium.ofdvz.classes.attacker.Zombie;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class OFEffect {

    public int id;
    int counter = 0;

    public OFEffect(Player victim, int skillDamage, int timeSpace, String message) {
        id = Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            victim.damage(skillDamage / timeSpace);
            victim.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                    ChatColor.translateAlternateColorCodes('&', message))
            );
            if (counter++ == Zombie.timeSpace) {
                Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                    Zombie.immunityCounter.remove(this);
                }, Zombie.immunityCooldown * 20);

                Bukkit.getScheduler().cancelTask(id);
            }
        }, 20L, 20L);
    }
}
