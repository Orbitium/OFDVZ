package me.orbitium.oflobby.classes.hero;

import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.UsageLimiter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class VoidWalker extends OFClass {

    UsageLimiter limiter;
    int maxRange;
    int maxYRange;

    public VoidWalker() {
        super("Voidwalker sea7", ClassType.Heroes);

        configCommend("# Maximum of teleport range (as blocks)");
        registerNewConfig("maximumSkillRange", "5");

        configCommend("# Maximum of teleport range for increasing Y level (as blocks)");
        registerNewConfig("maximumSkillYRange", "5");

        configCommend("# Teleport skill cooldown");
        registerNewConfig("skillCooldown", "5");

        registerNewEvent(PlayerSwapHandItemsEvent.class, (e) -> {
            PlayerSwapHandItemsEvent event = (PlayerSwapHandItemsEvent) e;
            event.setCancelled(true);
            Player player = event.getPlayer();
            Location l = player.getLocation();

            if (!limiter.check(player))
                return;
            limiter.update(player);

            Vector vector = player.getFacing().getDirection();
            float yaw = player.getLocation().getYaw();
            float pitch = player.getLocation().getPitch();

            double x = vector.getX();
            double z = vector.getZ();

            for (int i = maxRange; i > 0; i--) {
                Block block = l.getWorld().getHighestBlockAt(l.clone().add(x * i, 0, z * i));
                if (player.getLocation().getY() + maxYRange >= block.getLocation().getY()) {
                    player.teleport(block.getLocation().add(0, 1, 0).setDirection(vector));
                    player.getLocation().setPitch(pitch);
                    player.getLocation().setYaw(yaw);
                    return;
                }
            }
        });

        loadConfig();

        limiter = new UsageLimiter(Integer.parseInt(getCustomConfig("skillCooldown")));
        maxRange = Integer.parseInt(getCustomConfig("maximumSkillRange"));
    }
}
