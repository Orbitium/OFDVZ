package me.orbitium.oflobby.classes.defender;

import me.orbitium.oflobby.CropManager;
import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.ItemManager;
import me.orbitium.oflobby.util.ParticleUtil;
import me.orbitium.oflobby.util.UsageLimiter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class Baker extends OFClass {

    private int growTime = 5;
    private int breadConsuption = 5;
    private int cooldown = 5;
    private ItemStack food;
    private UsageLimiter limiter;

    public Baker() {
        super("Baker", ClassType.Defender);

        configCommend("# Wheat grow time (as second)");
        registerNewConfig("wheatGrowTime", "10");

        configCommend("# How much bread will be used for one food");
        registerNewConfig("wheatConsumption", "3");

        configCommend("# The cooldown of crafting food");
        registerNewConfig("craftFoodCooldown", "10");

        configCommend("# Food type");
        registerNewConfig("foodType", "BREAD");

        registerNewEvent(BlockPlaceEvent.class, (e) -> {
            BlockPlaceEvent event = (BlockPlaceEvent) e;
            if (event.getBlock().getType().equals(Material.WHEAT))
                CropManager.addQueue(event.getBlock());
        });

        registerNewEvent(PlayerInteractEvent.class, (e) -> {
            PlayerInteractEvent event = (PlayerInteractEvent) e;

            if (event.getClickedBlock() == null || !event.getClickedBlock().getType().equals(Material.FURNACE))
                return;

            if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.WHEAT)
                return;

            Player player = event.getPlayer();
            Location location = event.getClickedBlock().getLocation();

            String str = food.getType().name();

            if (ItemManager.convertItem(event.getPlayer(), breadConsuption, food, limiter, str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase(Locale.US), breadConsuption + " x Wheat"))
                ParticleUtil.spawnGreenParticle(player, location.add(0, 1, 0));
            else
                ParticleUtil.spawnRedParticle(player, location.add(0, 1, 0));
        });


        registerNewEvent(BlockBreakEvent.class, e -> {
            BlockBreakEvent event = (BlockBreakEvent) e;
            if (event.getBlock().getType().equals(Material.WHEAT) && !CropManager.crops.contains(event.getBlock())) {
                Location location = event.getBlock().getLocation();
                location.getWorld().dropItemNaturally(location, new ItemStack(Material.WHEAT));
                location.getWorld().dropItemNaturally(location, new ItemStack(Material.WHEAT_SEEDS, 1 + OFDVZ.random.nextInt(2)));
            }
        });

        loadConfig();
        growTime = Integer.parseInt(getCustomConfig("wheatGrowTime"));
        breadConsuption = Integer.parseInt(getCustomConfig("wheatConsumption"));
        cooldown = Integer.parseInt(getCustomConfig("craftFoodCooldown"));

        Material material = Material.getMaterial(getCustomConfig("foodType"));
        food = new ItemStack(material);

        limiter = new UsageLimiter(cooldown);

        int t = growTime * 20 / 8;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), CropManager::tick, t, t);
    }
}
