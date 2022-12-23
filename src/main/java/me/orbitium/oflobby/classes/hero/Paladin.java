package me.orbitium.oflobby.classes.hero;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.MessageUtil;
import me.orbitium.oflobby.util.UsageLimiter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Paladin extends OFClass {

    UsageLimiter limiter;
    int classRegenCooldown;
    int classRegenAmount;

    int range;
    int regenTime;
    int speedTime;
    int regenAmplifier;
    int speedAmplifier;

    PotionEffect regen;
    PotionEffect speed;

    public Paladin() {
        super("Kiki The Paladin", ClassType.Heroes);

        configCommend("# How much health will be regen as passive");
        registerNewConfig("classRegenAmount", "2");

        configCommend("# Regen time space as passive. For example if that's is 6");
        configCommend("# Player will regen 2 heal (1 hearth) in every 6 second");
        registerNewConfig("classRegenTimeSpace", "6");

        configCommend("The cooldown of skill called commanding (as seconds)");
        registerNewConfig("skillCooldown", "120");

        configCommend("The skill range (as blocks)");
        registerNewConfig("skillRange", "10");

        configCommend("The skill's regen increase time (as seconds)");
        registerNewConfig("skillRegenTime", "10");

        configCommend("The skill's speed increase time (as seconds)");
        registerNewConfig("skillSpeedTime", "10");

        configCommend("The skill's regen amplifier");
        registerNewConfig("skillRegenAmplifier", "1");

        configCommend("The skill's regen amplifier");
        registerNewConfig("skillSpeedAmplifier", "1");

        registerNewEvent(PlayerSwapHandItemsEvent.class, (e) -> {
            PlayerSwapHandItemsEvent event = (PlayerSwapHandItemsEvent) e;
            event.setCancelled(true);
            Player player = event.getPlayer();

            if (limiter.check(player)) {
                limiter.update(player);

                for (Entity nearbyEntity : player.getNearbyEntities(range, range, range)) {
                    if (!(nearbyEntity instanceof Player dr))
                        continue;

                    PersistentDataContainer pdc = dr.getPersistentDataContainer();

                    if (!pdc.has(OFDVZ.zombieKey, PersistentDataType.INTEGER) && pdc.has(OFDVZ.classKey, PersistentDataType.INTEGER)) {
                        dr.addPotionEffect(speed);
                        dr.addPotionEffect(regen);
                        if (dr != player)
                            MessageUtil.sendActionBarMessage(dr, "messages.paladinTeammateBuffMessage", "[seconds]", regenTime + "");
                    }
                }
            }
        });

        loadConfig();

        limiter = new UsageLimiter(Integer.parseInt(getCustomConfig("skillCooldown")));

        classRegenAmount = Integer.parseInt(getCustomConfig("classRegenAmount"));
        classRegenCooldown = Integer.parseInt(getCustomConfig("classRegenTimeSpace"));

        range = Integer.parseInt(getCustomConfig("skillRange"));
        regenTime = Integer.parseInt(getCustomConfig("skillRegenTime"));
        regenAmplifier = Integer.parseInt(getCustomConfig("skillSpeedTime"));
        speedTime = Integer.parseInt(getCustomConfig("skillRegenAmplifier"));
        speedAmplifier = Integer.parseInt(getCustomConfig("skillSpeedAmplifier"));

        Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            for (Player player : registeredPlayers) {
                AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                player.setHealth(Math.min(maxHealth.getValue(), player.getHealth() + classRegenAmount));
            }
        }, classRegenCooldown * 20, classRegenCooldown * 20);

        speed = new PotionEffect(PotionEffectType.SPEED, speedTime * 20, speedAmplifier);
        regen = new PotionEffect(PotionEffectType.REGENERATION, regenTime * 20, regenAmplifier);
    }

}
