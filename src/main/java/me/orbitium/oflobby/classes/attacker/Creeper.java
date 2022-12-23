package me.orbitium.oflobby.classes.attacker;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.ItemBuilder;
import me.orbitium.oflobby.util.UsageLimiter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Creeper extends OFClass {

    int skillCooldown;
    int range;
    ItemStack explodeItem;
    UsageLimiter limiter;
    NamespacedKey key;

    public Creeper() {
        super("Creeper", ClassType.Zombies);

        key = new NamespacedKey(OFDVZ.getInstance(), "exlodeItem");



        configCommend("# Skill cooldown AFTER respawn");
        registerNewConfig("skillCooldown", "30");

        configCommend("# Explosion range like 5x5x5");
        registerNewConfig("explodeRange", "5");

        configCommend("# Skill item material type");
        registerNewConfig("skillItemMaterial", "GUNPOWDER");

        configCommend("# The name of skill item");
        registerNewConfig("skillItemName", "Explode yourself");

        registerNewEvent(PlayerInteractEvent.class, (e) -> {
            PlayerInteractEvent event = (PlayerInteractEvent) e;
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            if (!itemStack.hasItemMeta())
                return;
            PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
            if (pdc.has(key, PersistentDataType.INTEGER)) {
                Player player = event.getPlayer();
                ((org.bukkit.entity.Creeper) player.getWorld().spawnEntity(player.getLocation(), EntityType.CREEPER)).explode();
            }
        });

        /*
        /gamerule mobGriefing false
         Difficulty can't be peaceful
         */

        loadConfig();

        range = Integer.parseInt(getCustomConfig("explodeRange"));

        Material material = Material.getMaterial(getCustomConfig("skillItemMaterial"));
        ItemBuilder itemBuilder = new ItemBuilder(material, getCustomConfig("skillItemName", false));
        explodeItem = itemBuilder.build(true);

        ItemMeta itemMeta = explodeItem.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
        itemMeta.getPersistentDataContainer().set(ItemBuilder.key, PersistentDataType.INTEGER, 0);
        explodeItem.setItemMeta(itemMeta);

        skillCooldown = Integer.parseInt(getCustomConfig("skillCooldown"));

        limiter = new UsageLimiter(skillCooldown);
        starterItems.add(explodeItem);
    }

    @Override
    public boolean addPlayer(Player player, boolean forceByAdmin) {
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.CREEPER);
        DisguiseAPI.disguiseEntity(player, mobDisguise);
        return super.addPlayer(player, forceByAdmin);
    }
}
