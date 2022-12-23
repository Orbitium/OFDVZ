package me.orbitium.ofdvz.classes.hero;

import me.orbitium.ofdvz.OFDVZ;
import me.orbitium.ofdvz.OFEffect;
import me.orbitium.ofdvz.classes.root.ClassType;
import me.orbitium.ofdvz.classes.root.OFClass;
import me.orbitium.ofdvz.util.ItemBuilder;
import me.orbitium.ofdvz.util.UsageLimiter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Tiger extends OFClass {

    UsageLimiter limiter;
    int pushSpeed;
    PotionEffect speed;

    int bleedingDamage;
    int bleedingTimeSpace;

    ItemStack customWeapon;
    NamespacedKey customWeaponKey;

    public Tiger() {
        super("Tehlone Tiger", ClassType.Heroes);

        customWeaponKey = new NamespacedKey(OFDVZ.getInstance(), "tigerWeapon");

        configCommend("# What's the amplifier of speed effect on Tehlone Tiger players");
        registerNewConfig("speedAmplifier", "1");

        configCommend("# What's the type of custom weapon");
        registerNewConfig("weaponMaterial", "IRON_SWORD");

        configCommend("# What's the name of custom weapon");
        registerNewConfig("weaponName", "Custom Weapon");

        configCommend("# What's the damage of the `bleeding`, same as the infection");
        configCommend("# There is no `immunity` for bleeding.");
        configCommend("# Bleeding won't be stacked.");
        registerNewConfig("bleedingDamage", "4");

        configCommend("# Same as the infection");
        registerNewConfig("bleedingDamageSpace", "4");

        configCommend("# The cooldown of push skill");
        registerNewConfig("skillCooldown", "10");

        configCommend("# Push skill's push speed");
        registerNewConfig("skillPushSpeed", "3");

        registerNewEvent(EntityDamageByEntityEvent.class, (e) -> {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
            if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim))
                return;

            if (!attacker.getInventory().getItemInMainHand().hasItemMeta())
                return;
            if (attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(customWeaponKey, PersistentDataType.INTEGER))
                new OFEffect(victim, bleedingDamage, bleedingTimeSpace, OFDVZ.getInstance().getConfig().getString(
                        "messages.youAreBleeding"));
        });

        registerNewEvent(PlayerSwapHandItemsEvent.class, (e) -> {
            PlayerSwapHandItemsEvent event = (PlayerSwapHandItemsEvent) e;
            Player player = event.getPlayer();

            if (!limiter.check(player))
                return;
            limiter.update(player);
            player.setVelocity(player.getLocation().getDirection().multiply(pushSpeed));
        });

        registerNewEvent(EntityDamageEvent.class, (e) -> {
            EntityDamageEvent event = (EntityDamageEvent) e;
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL)
                event.setCancelled(true);
        });

        loadConfig();

        limiter = new UsageLimiter(Integer.parseInt(getCustomConfig("skillCooldown")));
        pushSpeed = Integer.parseInt(getCustomConfig("skillPushSpeed"));
        speed = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, Integer.parseInt(getCustomConfig("speedAmplifier")));

        bleedingDamage = Integer.parseInt(getCustomConfig("bleedingDamage"));
        bleedingTimeSpace = Integer.parseInt(getCustomConfig("bleedingDamageSpace"));

        ItemBuilder itemBuilder = new ItemBuilder(Material.getMaterial(getCustomConfig("weaponMaterial")),
                getCustomConfig("weaponName"));
        customWeapon = itemBuilder.build(true);
        ItemMeta itemMeta = customWeapon.getItemMeta();
        itemMeta.getPersistentDataContainer().set(customWeaponKey, PersistentDataType.INTEGER, 0);
        customWeapon.setItemMeta(itemMeta);

        starterItems.add(customWeapon);
    }
}
