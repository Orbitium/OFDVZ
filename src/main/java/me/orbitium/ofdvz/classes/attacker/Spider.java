package me.orbitium.ofdvz.classes.attacker;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.orbitium.ofdvz.OFDVZ;
import me.orbitium.ofdvz.classes.root.ClassType;
import me.orbitium.ofdvz.classes.root.OFClass;
import me.orbitium.ofdvz.util.UsageLimiter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.ArrayList;
import java.util.List;

public class Spider extends OFClass {

    UsageLimiter limiter;
    int cooldown;
    int deleteCooldown;
    int maxDistance;
    int range;
    public static NamespacedKey explodeKey;

    public Spider() {
        super("Spider", ClassType.Zombies);

        explodeKey = new NamespacedKey(OFDVZ.getInstance(), "explodeListener");

        configCommend("# Spider's max health");
        registerNewConfig("maxHealth", "10");



        configCommend("# Skill cooldown");
        registerNewConfig("skillCooldown", "1");

        configCommend("# Skill range, max block distance for using skill");
        registerNewConfig("skillRange", "10");

        configCommend("# Vine delete delay (as seconds)");
        registerNewConfig("vineDeleteDelay", "30");

        registerNewEvent(PlayerSwapHandItemsEvent.class, (e) -> {
            PlayerSwapHandItemsEvent event = (PlayerSwapHandItemsEvent) e;
            event.setCancelled(true);
            Player player = event.getPlayer();

            if (limiter.check(player)) {
                limiter.update(player);

                player.launchProjectile(Snowball.class, player.getEyeLocation().getDirection().multiply(5));
            }
        });


        registerNewEvent(ProjectileHitEvent.class, (e) -> {
            ProjectileHitEvent event = (ProjectileHitEvent) e;
            if (!(event.getEntity().getShooter() instanceof Player shooter))
                return;

            if (event.getHitEntity() != null && event.getEntity().getLocation().distance(shooter.getLocation()) <= maxDistance) {
                Entity hitEntity = event.getHitEntity();
                Block block = event.getHitEntity().getWorld().getBlockAt(hitEntity.getLocation());
                block.setType(Material.COBWEB);

                Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                    block.setType(Material.AIR);
                }, deleteCooldown);
            } else if (event.getHitBlock() != null && event.getHitBlock().getLocation().distance(shooter.getLocation()) <= maxDistance) {
                Player player = (Player) event.getEntity().getShooter();
                World world = player.getWorld();
                Block block = event.getHitBlock();

                Block max = world.getBlockAt(world.getHighestBlockAt(block.getLocation().add(0,1,0)).getLocation());
                BlockFace targetFace = event.getHitBlockFace();

                if (targetFace == BlockFace.UP || targetFace == BlockFace.DOWN)
                    return;

                int x = max.getX();
                int z = max.getZ();

                List<Block> blocks = new ArrayList<>();

                for (int y = max.getY(); y > 60; y--) {
                    Location l = world.getBlockAt(x, y, z).getLocation().toVector().add(targetFace.getDirection().toBlockVector()).toLocation(world);
                    Block vine = world.getBlockAt(l);
                    if (vine.getType() != Material.AIR)
                        break;
                    vine.setType(Material.VINE);
                    blocks.add(vine);

                    MultipleFacing mf = (MultipleFacing) vine.getState().getBlockData();
                    mf.setFace(targetFace.getOppositeFace(), true);
                    vine.setBlockData(mf, true);

                }

                Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                    for (Block block1 : blocks) {
                        block1.setType(Material.AIR);
                    }
                }, deleteCooldown * 20);

            }

        });

        loadConfig();

        cooldown = Integer.parseInt(getCustomConfig("skillCooldown"));
        deleteCooldown = Integer.parseInt(getCustomConfig("vineDeleteDelay"));
        maxDistance = Integer.parseInt(getCustomConfig("skillRange"));
        limiter = new UsageLimiter(cooldown);
    }

    @Override
    public boolean addPlayer(Player player, boolean forceByAdmin) {
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.SPIDER);
        DisguiseAPI.disguiseEntity(player, mobDisguise);
        return super.addPlayer(player, forceByAdmin);
    }
}
