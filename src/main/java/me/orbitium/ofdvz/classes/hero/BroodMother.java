package me.orbitium.ofdvz.classes.hero;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.orbitium.ofdvz.OFDVZ;
import me.orbitium.ofdvz.classes.root.ClassType;
import me.orbitium.ofdvz.classes.root.OFClass;
import me.orbitium.ofdvz.listener.BlockListener;
import me.orbitium.ofdvz.util.UsageLimiter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.persistence.PersistentDataType;

public class BroodMother extends OFClass {

    UsageLimiter limiter;
    int xz;
    int yr;
    int range;
    int silverfishFromBlocks;
    int silverfishFromMother;

    public BroodMother() {
        super("Brood Mother", ClassType.Heroes);

        configCommend("# The cooldown of skill");
        registerNewConfig("skillCooldown", "20");

        configCommend("# The X and Z range of spawn infect blocks (as blocks)");
        registerNewConfig("skillXZRange", "3");

        configCommend("# The Y range of spawn infect blocks (as blocks)");
        registerNewConfig("skillYRange", "1");

        configCommend("# The range for spawning silverfish (as blocks and each side)");
        registerNewConfig("skillRange", "10");

        configCommend("# Max silverfish which spawned breaking blocks");
        registerNewConfig("maxSilverfishFromBlocks", "5");

        configCommend("# Max silverfish which spawned from BroodMother");
        registerNewConfig("maxSilverfishFromBroodMother", "5");

        registerNewEvent(PlayerSwapHandItemsEvent.class, e -> {
            PlayerSwapHandItemsEvent event = (PlayerSwapHandItemsEvent) e;
            Player player = event.getPlayer();

            if (!limiter.check(player))
                return;

            limiter.update(player);

            Location l = player.getLocation();
            World world = l.getWorld();

            int counter = 0;

            for (int x = l.getBlockX() - xz; x < l.getBlockX() + xz; x++) {
                for (int z = l.getBlockZ() - xz; z < l.getBlockZ() + xz; z++) {
                    for (int y = l.getBlockY() - yr; y < l.getBlockY() + yr; y++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (BlockListener.breakableBlocks.contains(block.getType())) {
                            if (counter++ <= silverfishFromBlocks) {
                                block.setType(Material.AIR);
                                world.spawnEntity(block.getLocation(), EntityType.SILVERFISH);
                            }
                        }
                    }
                }
            }

            for (int i = silverfishFromMother; i > 0; i--)
                player.getWorld().spawnEntity(player.getLocation().add(i * 0.1, 0, i * 0.1), EntityType.SILVERFISH);

        });

        loadConfig();

        limiter = new UsageLimiter(Integer.parseInt(getCustomConfig("skillCooldown")));
        xz = Integer.parseInt(getCustomConfig("skillXZRange"));
        yr = Integer.parseInt(getCustomConfig("skillYRange"));
        range = Integer.parseInt(getCustomConfig("skillRange"));
        silverfishFromBlocks = Integer.parseInt(getCustomConfig("maxSilverfishFromBlocks"));
        silverfishFromMother = Integer.parseInt(getCustomConfig("maxSilverfishFromBroodMother"));
    }

    @Override
    public boolean addPlayer(Player player, boolean forceByAdmin) {
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.SILVERFISH);
        DisguiseAPI.disguiseEntity(player, mobDisguise);
        player.getPersistentDataContainer().set(OFDVZ.zombieKey, PersistentDataType.INTEGER, 0);
        return super.addPlayer(player, forceByAdmin);
    }
}
