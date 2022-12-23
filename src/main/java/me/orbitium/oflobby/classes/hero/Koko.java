package me.orbitium.oflobby.classes.hero;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.classes.root.ClassManager;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Koko extends OFClass {

    ItemStack lute;
    UsageLimiter limiter;
    int healthIncrease;
    int healthTime;
    int healthRange;
    NamespacedKey key;
    String message;

    public Koko() {
        super("Koko", ClassType.Heroes);

        message = OFDVZ.getInstance().getConfig().getString("messages.yourHealthIsIncreasedByBard");

        key = new NamespacedKey(OFDVZ.getInstance(), "kokoKey");

        configCommend("# How much health will be gain temporary when skill usage");
        registerNewConfig("healthIncrease", "10");

        configCommend("# How much time later will be deleted temporary healths (as secodns)");
        registerNewConfig("healthTime", "30");

        configCommend("# What's the range of skill (as blocks)");
        registerNewConfig("healthSkillRange", "3");

        configCommend("# What's the cooldown of skill (as seconds)");
        registerNewConfig("skillCooldown", "300");

        configCommend("# What's the type of lute weapon");
        registerNewConfig("luteMaterial", "STICK");

        configCommend("# What's the name of lute weapon");
        registerNewConfig("luteName", "Lute");

        registerNewEvent(PlayerInteractEvent.class, (e) -> {
            PlayerInteractEvent event = (PlayerInteractEvent) e;
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player bard = event.getPlayer();

                if (!bard.getInventory().getItemInMainHand().hasItemMeta() || !bard.getInventory().getItemInMainHand().getItemMeta()
                        .getPersistentDataContainer().has(key, PersistentDataType.INTEGER))
                    return;

                if (!limiter.check(bard))
                    return;

                limiter.update(bard);
                for (Entity nearbyEntity : bard.getNearbyEntities(healthRange, healthRange, healthRange)) {
                    if ((nearbyEntity instanceof Player player)) {
                        PersistentDataContainer pdc = player.getPersistentDataContainer();

                        if (!pdc.has(OFDVZ.zombieKey, PersistentDataType.INTEGER) && pdc.has(OFDVZ.classKey, PersistentDataType.INTEGER)) {
                            AttributeInstance a = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(a.getValue() + healthIncrease);
                            Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                                OFClass c = ClassManager.getClassFromPlayer(player);
                                for (Player registeredPlayer : c.registeredPlayers) {
                                    registeredPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(c.maxHealth);
                                }
                            }, healthTime * 20);
                            MessageUtil.sendActionBarMessage(player, "messages.kokoTeammateBuffMessage", "[seconds]", healthTime + "");
                        }
                    }
                }
            }
        });

        registerNewEvent(EntityDamageByEntityEvent.class, e -> {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;

            Player attacker = (Player) event.getDamager();

            if (!attacker.getInventory().getItemInMainHand().hasItemMeta() || !attacker.getInventory().getItemInMainHand()
                    .getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER))
                return;

            if (!(event.getEntity() instanceof LivingEntity victim))
                return;

            for (Entity nearbyEntity : victim.getNearbyEntities(1, 2, 1)) {
                if (!(nearbyEntity instanceof LivingEntity entity))
                    return;
                if (entity != attacker)
                    entity.damage(victim.getLastDamage());
            }
        });

        loadConfig();

        limiter = new UsageLimiter(Integer.parseInt(getCustomConfig("skillCooldown")));
        healthIncrease = Integer.parseInt(getCustomConfig("healthIncrease"));
        healthTime = Integer.parseInt(getCustomConfig("healthTime"));
        healthRange = Integer.parseInt(getCustomConfig("healthSkillRange"));

        ItemBuilder itemBuilder = new ItemBuilder(Material.getMaterial(getCustomConfig("luteMaterial")),
                getCustomConfig("luteName"));
        lute = itemBuilder.build(true);
        ItemMeta itemMeta = lute.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
        itemMeta.getPersistentDataContainer().set(ItemBuilder.key, PersistentDataType.INTEGER, 0);
        lute.setItemMeta(itemMeta);
        starterItems.add(lute);
    }
}
