package me.orbitium.oflobby.classes.attacker;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.OFEffect;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class Zombie extends OFClass {

    public static Map<OFEffect, Player> immunityCounter = new HashMap<>();
    public static int timeSpace = 1;
    public static int skillDamage = 0;
    ItemStack claw;
    NamespacedKey key;

    public static int immunityCooldown = 0;

    public Zombie() {
        super("Zombie", ClassType.Zombies);

        key = new NamespacedKey(OFDVZ.getInstance(), "claw");

        configCommend("# What's the damage of the `infection`");
        registerNewConfig("infectionDamage", "4");

        configCommend("# Infection damage space. For example if the time space is 4 seconds.");
        configCommend("# Damage will be divine for 4 seconds. If infection will give 8 damage");
        configCommend("# Defender will take 2 damage per second");
        registerNewConfig("infectionDamageSpace", "4");

        configCommend("# If a defender infected by zombie soon, he will have immunity.");
        configCommend("# Basically: Any defender can be able to infected after one minute after an infection.");
        configCommend("# If that config is 0, the effect won't be stacked.");
        registerNewConfig("infectionImmunity", "60");

        configCommend("# What's the claw material");
        registerNewConfig("clawMaterial", "WOODEN_SWORD");

        configCommend("# What's the name of the claw (It can be empty)");
        registerNewConfig("clawName", "Claw");

        registerNewEvent(EntityDamageByEntityEvent.class, (e) -> {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;

            if (event.getDamager() instanceof Player attacker) {
                if (!attacker.getInventory().getItemInMainHand().hasItemMeta() || !(event.getEntity() instanceof Player victim))
                    return;

                if (!attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER))
                    return;

                if (immunityCounter.containsValue(victim))
                    return;

                immunityCounter.put(new OFEffect(victim, skillDamage, timeSpace,
                        OFDVZ.getInstance().getConfig().getString("messages.youAreInfected")), victim);
            }
        });

        loadConfig();

        immunityCooldown = Integer.parseInt(getCustomConfig("infectionImmunity"));
        skillDamage = Integer.parseInt(getCustomConfig("infectionDamage"));
        timeSpace = Integer.parseInt(getCustomConfig("infectionDamageSpace"));

        Material material = Material.getMaterial(getCustomConfig("clawMaterial"));
        ItemBuilder itemBuilder = new ItemBuilder(material, getCustomConfig("clawName"));
        claw = itemBuilder.build(true);

        ItemMeta itemMeta = claw.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
        itemMeta.getPersistentDataContainer().set(ItemBuilder.key, PersistentDataType.INTEGER, 0);
        claw.setItemMeta(itemMeta);

        starterItems.add(claw);
    }

    @Override
    public boolean addPlayer(Player player, boolean forceByAdmin) {
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.ZOMBIE);
        DisguiseAPI.disguiseEntity(player, mobDisguise);
        return super.addPlayer(player, forceByAdmin);
    }
}
