package me.orbitium.ofdvz.classes.attacker;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.orbitium.ofdvz.classes.root.ClassType;
import me.orbitium.ofdvz.classes.root.OFClass;
import me.orbitium.ofdvz.util.UsageLimiter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class Skeleton extends OFClass {

    UsageLimiter limiter;
    ItemStack arrow;
    int cooldown;
    int arrowSpawnAmount;

    int maxHealth;

    public Skeleton() {
        super("Skeleton", ClassType.Zombies);

        configCommend("# Skeleton's max health");
        registerNewConfig("maxHealth", "10");



        configCommend("# Arrow spawn skill cooldown");
        registerNewConfig("skillCooldown", "10");

        configCommend("# How much arrow will be spawned per skill usage");
        registerNewConfig("arrowSpawnAmount", "10");

        registerNewEvent(PlayerSwapHandItemsEvent.class, (e) -> {
            PlayerSwapHandItemsEvent event = (PlayerSwapHandItemsEvent) e;
            if (event.getOffHandItem() == null || !event.getOffHandItem().getType().equals(Material.BOW))
                return;
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (limiter.check(player)) {
                limiter.update(player);
                player.getInventory().addItem(arrow);
            }
        });

        loadConfig();
        cooldown = Integer.parseInt(getCustomConfig("skillCooldown"));
        arrowSpawnAmount = Integer.parseInt(getCustomConfig("arrowSpawnAmount"));
        maxHealth = Integer.parseInt(getCustomConfig("maxHealth"));
        arrow = new ItemStack(Material.ARROW, arrowSpawnAmount);
        limiter = new UsageLimiter(cooldown);
    }

    @Override
    public boolean addPlayer(Player player, boolean forceByAdmin) {
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.SKELETON);
        DisguiseAPI.disguiseEntity(player, mobDisguise);
        return super.addPlayer(player, forceByAdmin);
    }
}
