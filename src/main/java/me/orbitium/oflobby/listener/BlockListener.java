package me.orbitium.oflobby.listener;

import me.orbitium.oflobby.CompassManager;
import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.beacon.BeaconListener;
import me.orbitium.oflobby.classes.defender.Alchemist;
import me.orbitium.oflobby.classes.defender.Lumberjack;
import me.orbitium.oflobby.classes.root.ClassManager;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.database.CCache;
import me.orbitium.oflobby.event.Events;
import me.orbitium.oflobby.util.ItemManager;
import me.orbitium.oflobby.util.MessageUtil;
import me.orbitium.oflobby.util.Time;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;

public class BlockListener implements Listener {

    public static List<Material> breakableBlocks = new ArrayList<>();
    static int spawnerProtectionRange;


    public BlockListener() {
        List<String> b = OFDVZ.getInstance().getConfig().getStringList("breakableBlocks");
        for (String s : b) {
            Material material = Material.getMaterial(s.toUpperCase(Locale.US));
            if (material == null)
                OFDVZ.getInstance().getLogger().log(Level.SEVERE, "Breakable block called " + s + " couldn't found!");
            breakableBlocks.add(material);
        }
        spawnerProtectionRange = OFDVZ.getInstance().getConfig().getInt("spawner.spawnerProtectionRange");
    }

    @EventHandler
    public void onGravelBreak(BlockBreakEvent event) {
        if (event.getBlock().getType().equals(OFDVZ.emergencyBlock))
            return;
        if (!breakableBlocks.contains(event.getBlock().getType()) && !event.getPlayer().isOp())
            event.setCancelled(true);
        else
            event.setDropItems(false);
        CCache.runClassOnEvent(event.getPlayer(), event);
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COMPASS)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                CompassManager.next(event.getPlayer());
            else
                CompassManager.t(event.getPlayer());
        }
        CCache.runClassOnEvent(event.getPlayer(), event);

        if (event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.BREWING_STAND))
            return;

        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (itemStack.hasItemMeta()) {
            PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
            if (pdc.has(Alchemist.drinkRemainKey, PersistentDataType.INTEGER)) {
                int n = Time.dateToInt();
                int c = Alchemist.drinkCooldownCounter.getOrDefault(event.getPlayer(), 0);
                if (c + Alchemist.drinkCooldown > n) {
                    MessageUtil.sendMessageToPlayer(event.getPlayer(), "messages.error.aleDrinkCooldownIsNotFinished",
                            "[remain]", (c + Alchemist.drinkCooldown - n) + "");
                    return;
                }

                ClassManager.alchemist.drink(event.getPlayer());
            } else if (pdc.has(Lumberjack.key, PersistentDataType.INTEGER)) {
                if (event.getClickedBlock() == null)
                    return;
                Location baseBlock = event.getClickedBlock().getLocation();
                World world = event.getClickedBlock().getWorld();
                event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                int range = Lumberjack.range;
                for (int x = baseBlock.getBlockX() - range; x < baseBlock.getBlockX() + range; x++) {
                    for (int y = baseBlock.getBlockY() - range; y < baseBlock.getBlockY() + range; y++) {
                        for (int z = baseBlock.getBlockZ() - range; z < baseBlock.getBlockZ() + range; z++) {
                            Block block = world.getBlockAt(x, y, z);
                            if (block.getType().equals(Material.COBBLESTONE))
                                block.setType(Lumberjack.newMaterial);
                        }
                    }
                }
            }
        }

        if (event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.END_STONE)) {
            if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BOWL)) {
                ItemManager.convertItem(event.getPlayer(), Lumberjack.bowlPerMortar, Lumberjack.mortar, Lumberjack.mortarLimiter, "mortar", Lumberjack.bowlPerMortar + "x bowl");
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getView().getTopInventory().getSize() == 5 || event.getView().getTopInventory().getSize() == 3
                || event.getView().getTopInventory().getSize() == 10)
            event.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;

        if (event.getCurrentItem().getType().equals(Material.DIAMOND)) {
            Player player = (Player) event.getWhoClicked();

            boolean used = false;

            if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() != Material.DIAMOND_HELMET) {
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                used = true;
            }

            if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() != Material.DIAMOND_CHESTPLATE) {
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                used = true;
            }

            if (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() != Material.DIAMOND_LEGGINGS) {
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                used = true;
            }

            if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() != Material.DIAMOND_BOOTS) {
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                used = true;
            }

            if (used)
                event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
        }
    }

    @EventHandler
    public void cancelDrink(PlayerItemConsumeEvent event) {
        if (event.getItem().getType().equals(Material.POTION))
            event.setCancelled(true);
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        for (Entity e : event.getPlayer().getWorld().getNearbyEntities(event.getBlockPlaced().getLocation(), BeaconListener.beaconRange,
                BeaconListener.beaconRange, BeaconListener.beaconRange)) {
            if (e.getType() == EntityType.ARMOR_STAND) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                    return;
                } else
                    break;
            }
        }
        World world = event.getBlock().getWorld();
        int bx = event.getBlock().getX();
        int by = event.getBlock().getY();
        int bz = event.getBlock().getZ();
        for (int x = bx - spawnerProtectionRange; x < bx + spawnerProtectionRange; x++) {
            for (int y = by - spawnerProtectionRange; y < by + spawnerProtectionRange; y++) {
                for (int z = bz - spawnerProtectionRange; z < bz + spawnerProtectionRange; z++) {
                    if (world.getBlockAt(x, y, z).getType().equals(Material.SPAWNER)) {
                        if (!event.getPlayer().isOp()) {
                            event.setCancelled(true);
                            return;
                        } else
                            break;
                    }
                }
            }
        }
        CCache.runClassOnEvent(event.getPlayer(), event);
    }

    @EventHandler
    public void a(PlayerSwapHandItemsEvent event) {
        CCache.runClassOnEvent(event.getPlayer(), event);
    }

    @EventHandler
    public void b(ProjectileHitEvent event) {
        if (event.getHitEntity() != null && event.getHitEntity() instanceof Player victim) {
            if (event.getEntity().getShooter() instanceof Player attacker) {
                if (victim.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
                    if (attacker.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER)) {
                        event.setCancelled(true);
                        return;
                    }
                if (!victim.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
                    if (!attacker.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER)) {
                        event.setCancelled(true);
                        return;
                    }
            }
        }
        if (event.getEntity().getShooter() instanceof Player player)
            CCache.runClassOnEvent(player, event);
    }

    @EventHandler
    public void c(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper))
            return;
        else {
            for (Block block : event.blockList()) {
                if (breakableBlocks.contains(block.getType()))
                    block.setType(Material.AIR);
            }
        }
        event.setCancelled(true);
        event.getLocation().getWorld().playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0);
    }

    @EventHandler
    public void d(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        CCache.runClassOnEvent(player, event);

        PersistentDataContainer pdc = event.getEntity().getPersistentDataContainer();
        if (pdc.has(OFDVZ.classKey, PersistentDataType.INTEGER)) {
            if (player.getHealth() - event.getDamage() <= 0) {
                event.setCancelled(true);
                OFClass ofclass = ClassManager.getClassFromPlayer(player);
                ofclass.die(player);
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            }
        }
    }

    @EventHandler
    public void s(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Monster && !Events.isPlagueStarted) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Monster monster) {
            monster.setTarget(BeaconListener.center);
        }
    }
}
