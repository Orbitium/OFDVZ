package me.orbitium.oflobby.classes.hero;

import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.MessageUtil;
import me.orbitium.oflobby.util.UsageLimiter;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class Zachattck extends OFClass {

    int healthSteal;
    int skillCooldown;
    int skillRange;
    int skillPush;
    UsageLimiter limiter;

    public Zachattck() {
        super("Zachattck", ClassType.Heroes);

        configCommend("How much health will be stole per hit");
        configCommend("Check here: https://minecraft.fandom.com/wiki/Health");
        registerNewConfig("healthSteal", "1");

        configCommend("Skill push range like 3x3");
        registerNewConfig("skillPushRange", "3");

        configCommend("Skill push multiply");
        configCommend("Check here: https://www.spigotmc.org/wiki/vector-programming-for-beginners/");
        registerNewConfig("skillPushMultiply", "3");

        configCommend("Skill cooldown");
        registerNewConfig("skillCooldown", "30");

        registerNewEvent(EntityDamageByEntityEvent.class, (e) -> {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;

            if (!(event.getDamager() instanceof Player attacker))
                return;

            if (!registeredPlayers.contains(attacker))
                return;
            String name = attacker.getInventory().getItemInMainHand().getType().name();

            if (name.contains("AXE")) {
                if (!(event.getEntity() instanceof LivingEntity victim))
                    return;
                victim.damage(healthSteal);
                double maxHealth = attacker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                attacker.setHealth(Math.min(maxHealth, attacker.getHealth() + healthSteal));
                MessageUtil.sendActionBarMessage(attacker, "messages.healthStole");
            }
        });

        registerNewEvent(PlayerSwapHandItemsEvent.class, (e) -> {
            PlayerSwapHandItemsEvent event = (PlayerSwapHandItemsEvent) e;
            if (event.getOffHandItem() == null)
                return;
            if (!event.getOffHandItem().getType().name().contains("AXE"))
                return;

            event.setCancelled(true);

            Player player = event.getPlayer();

            if (limiter.check(player)) {
                limiter.update(player);

                for (Entity entity : player.getNearbyEntities(skillRange, skillRange, skillRange))
                    entity.setVelocity(entity.getLocation().getDirection().multiply(skillPush * -1));
            }
        });

        loadConfig();

        healthSteal = Integer.parseInt(getCustomConfig("healthSteal"));
        skillCooldown = Integer.parseInt(getCustomConfig("skillCooldown"));
        skillRange = Integer.parseInt(getCustomConfig("skillPushRange"));
        skillPush = Integer.parseInt(getCustomConfig("skillPushMultiply"));
        limiter = new UsageLimiter(skillCooldown);

    }
}
