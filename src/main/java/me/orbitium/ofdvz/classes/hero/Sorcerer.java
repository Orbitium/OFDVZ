package me.orbitium.ofdvz.classes.hero;

import me.orbitium.ofdvz.OFDVZ;
import me.orbitium.ofdvz.classes.root.ClassType;
import me.orbitium.ofdvz.classes.root.OFClass;
import me.orbitium.ofdvz.util.ItemBuilder;
import me.orbitium.ofdvz.util.UsageLimiter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Sorcerer extends OFClass {

    ItemStack wand;
    UsageLimiter leftLimiter;
    UsageLimiter rightLimiter;

    int range;
    public static NamespacedKey key;

    public Sorcerer() {
        super("BANG The Sorcerer", ClassType.Heroes);

        key = new NamespacedKey(OFDVZ.getInstance(), "wand");

        configCommend("# The name of the wand");
        registerNewConfig("wandName", "Wand");

        configCommend("# The material of wand");
        registerNewConfig("wandMaterial", "STICK");

        configCommend("# What's the cooldown of left click AKA explosion");
        registerNewConfig("explosionCooldown", "30");

        configCommend("# What's the cooldown of right click AKA flamethrower cone");
        registerNewConfig("flameThrowerCooldown", "30");

        configCommend("# What's the range of flamethrower (as blocks)");
        registerNewConfig("flameThrowerRange", "3");

        registerNewEvent(PlayerInteractEvent.class, (e) -> {
            PlayerInteractEvent event = (PlayerInteractEvent) e;
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            if (!itemStack.hasItemMeta())
                return;

            if (!itemStack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER))
                return;

            Action c = event.getAction();
            Player player = event.getPlayer();

            if (c == Action.LEFT_CLICK_AIR || c == Action.LEFT_CLICK_BLOCK) {
                if (!leftLimiter.check(player))
                    return;
                leftLimiter.update(player);
                Block block = player.getTargetBlockExact(32);
                if (block != null) {
                    Creeper creeper = (Creeper) player.getWorld().spawnEntity(block.getLocation().add(0, 1, 0), EntityType.CREEPER);
                    creeper.explode();
                }
            } else if (c == Action.RIGHT_CLICK_AIR || c == Action.RIGHT_CLICK_BLOCK) {
                if (!rightLimiter.check(player))
                    return;
                rightLimiter.update(player);

                Vector vector = player.getFacing().getDirection().clone();
                World world = player.getWorld();
                Location location = player.getLocation().clone();
                List<Block> a = new ArrayList<>();
                for (int i = 0; i < range; i++) {
                    int finalI = i;
                    Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                        Location location2 = location.clone().add(vector.clone().multiply(finalI + 1));
                        Block block = checkBlock(world, location2);
                        if (block != null)
                            a.add(block);
                        Location ld = location2.getBlock().getLocation().clone();
                        ld.setYaw(location2.getYaw() - 90);
                        Location rd = location2.getBlock().getLocation().clone();
                        rd.setYaw(location2.getYaw() + 90);
                        Vector left = ld.getDirection();
                        Vector right = rd.getDirection();
                        for (int j = finalI; j > 0; j--) {
                            Location lf = location2.clone().add(left.clone().multiply(j));
                            Location rf = location2.clone().add(right.clone().multiply(j));
                            //Block block1 = world.getBlockAt(lf).getLocation().getBlock();
                            //Block block2 = world.getBlockAt(rf).getLocation().getBlock();

                            Block block3 = checkBlock(world, world.getBlockAt(lf).getLocation());
                            a.add(block3);

                            Block block4 = checkBlock(world, world.getBlockAt(rf).getLocation());
                            a.add(block4);


                           /* block1.setType(Material.FIRE);
                            block2.setType(Material.FIRE);
                            a.add(block1);
                            a.add(block2); */
                        }
                        Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                            for (Block b : a) {
                                if (b != null)
                                    b.setType(Material.AIR);
                            }

                        }, 17 * (finalI + 1));
                    }, 3 * (i + 1));
                }
            }
        });

                            /*
                    for (int j = i; j > 0; j--) {
                        for (int z = 0; z < 4; z++) {
                            for (int y = 0; y < 3; y++) {
                                Location lf = location.clone().add(0, y, 0).add(left.clone().multiply(j + (z * 0.2)));
                                Location rf = location.clone().add(0, y, 0).add(right.clone().multiply(j + (z * 0.2)));
                                Location lf2 = location.clone().add(0, y, 0).add(left.clone().multiply(j + (z * -0.2)));
                                Location rf2 = location.clone().add(0, y, 0).add(right.clone().multiply(j + (z * -0.2)));
                                ParticleUtil.spawnRedParticle(lf);
                                ParticleUtil.spawnRedParticle(lf2);
                                ParticleUtil.spawnRedParticle(rf);
                                ParticleUtil.spawnRedParticle(rf2);
                            }
                        }
                    } */


        loadConfig();

        Material material = Material.getMaterial(getCustomConfig("wandMaterial"));
        ItemBuilder itemBuilder = new ItemBuilder(material, getCustomConfig("wandName"));
        wand = itemBuilder.build(true);

        ItemMeta itemMeta = wand.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
        itemMeta.getPersistentDataContainer().set(ItemBuilder.key, PersistentDataType.INTEGER, 0);
        wand.setItemMeta(itemMeta);

        starterItems.add(wand);

        leftLimiter = new UsageLimiter(Integer.parseInt(getCustomConfig("explosionCooldown")));
        rightLimiter = new UsageLimiter(Integer.parseInt(getCustomConfig("flameThrowerCooldown")));

        range = Integer.parseInt(getCustomConfig("flameThrowerRange"));
    }


    public Block checkBlock(World world, Location location2) {
        Block block = world.getBlockAt(location2).getLocation().getBlock();
        if (block.getType() == Material.AIR) {
            block.setType(Material.FIRE);
            return block;
        } else {
            block = world.getBlockAt(location2.clone().add(0, 1, 0)).getLocation().getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.FIRE);
                return block;
            }
        }

        return null;
    }
}
