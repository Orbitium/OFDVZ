package me.orbitium.ofdvz.classes.defender;

import me.orbitium.ofdvz.classes.root.ClassType;
import me.orbitium.ofdvz.classes.root.OFClass;
import me.orbitium.ofdvz.util.ItemManager;
import me.orbitium.ofdvz.util.ParticleUtil;
import me.orbitium.ofdvz.util.UsageLimiter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Blacksmith extends OFClass {

    int neededGoldNugget = -1;
    int neededGold = -1;
    int neededGoldForShield = -1;
    int neededPlanksForShield = -1;

    int craftGoldIngotCooldown = -1;
    int craftShieldCooldown = -1;
    int craftDiamondCooldown = -1;

    UsageLimiter goldCraftLimiter;
    UsageLimiter diamondCraftLimiter;
    UsageLimiter shieldCraftLimiter;

    ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT);
    ItemStack diamond = new ItemStack(Material.DIAMOND);
    ItemStack shield = new ItemStack(Material.SHIELD);

    public Blacksmith() {
        super("Blacksmith", ClassType.Defender);

        configCommend("# How many gold nugget need to craft golden ingot");
        registerNewConfig("neededGoldNuggetPerIngot", "3");

        configCommend("# How many gold need to craft diamond");
        registerNewConfig("neededGoldPerDiamond", "3");

        configCommend("# How many gold ingot need to craft shiled");
        registerNewConfig("neededGoldPerShield", "1");

        configCommend("# How many planks need to craft shield");
        registerNewConfig("neededPlanksForShield", "5");

        configCommend("# The cooldown of crafting gold ingot");
        registerNewConfig("craftGoldIngotCooldown", "10");

        configCommend("# The cooldown of crafting diamond");
        registerNewConfig("craftDiamondCooldown", "10");

        configCommend("# The cooldown of crafting shield");
        registerNewConfig("craftShieldCooldowm", "10");


        registerNewEvent(PlayerInteractEvent.class, (e) -> {
            PlayerInteractEvent event = (PlayerInteractEvent) e;
            Player player = event.getPlayer();

            if (event.getClickedBlock() == null)
                return;

            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            Material material = itemStack.getType();
            Location location = event.getClickedBlock().getLocation();

            if (event.getClickedBlock().getType().equals(Material.FURNACE)) {
                if (material.equals(Material.GOLD_NUGGET))
                    if (ItemManager.convertItem(event.getPlayer(), neededGoldNugget, goldIngot, goldCraftLimiter, "Gold Ingot", neededGold + "x Gold Nugget"))
                        ParticleUtil.spawnGreenParticle(player, event.getClickedBlock().getLocation());
                    else
                        ParticleUtil.spawnRedParticle(player, event.getClickedBlock().getLocation());
            } else if (event.getClickedBlock().getType().equals(Material.ANVIL)) {
                if (material.equals(Material.GOLD_INGOT))
                    if (ItemManager.convertItem(event.getPlayer(), neededGold, diamond, diamondCraftLimiter, "Diamond", neededGold + "x Gold Ingot"))
                        ParticleUtil.spawnGreenParticle(player, event.getClickedBlock().getLocation());
                    else
                        ParticleUtil.spawnRedParticle(player, event.getClickedBlock().getLocation());

                if (material.equals(Material.OAK_PLANKS)) {
                    int g = 0;
                    for (ItemStack content : player.getInventory().getContents()) {
                        if (content == null)
                            continue;
                        if (content.getType().equals(Material.GOLD_INGOT)) {
                            if (g + content.getAmount() >= neededGoldForShield) {
                                if (ItemManager.convertItem(player, neededPlanksForShield, shield, shieldCraftLimiter, "Shield", neededGoldForShield + "x Gold Ingot")) {
                                    ParticleUtil.spawnGreenParticle(player, event.getClickedBlock().getLocation());
                                    content.setAmount(Math.max(0, content.getAmount() - neededGoldForShield));
                                    if (g - neededGoldForShield > 0)
                                        player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, g - neededGold));
                                    return;
                                }
                            } else {
                                g += content.getAmount();
                                content.setAmount(0);
                            }
                        }
                    }
                    player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, g));
                }
            }
        });


        loadConfig();
        neededGoldNugget = Integer.parseInt(getCustomConfig("neededGoldNuggetPerIngot"));
        neededGold = Integer.parseInt(getCustomConfig("neededGoldPerDiamond"));
        neededGoldForShield = Integer.parseInt(getCustomConfig("neededGoldPerShield"));
        neededPlanksForShield = Integer.parseInt(getCustomConfig("neededPlanksForShield"));

        craftGoldIngotCooldown = Integer.parseInt(getCustomConfig("craftGoldIngotCooldown"));
        craftShieldCooldown = Integer.parseInt(getCustomConfig("craftShieldCooldowm"));
        craftDiamondCooldown = Integer.parseInt(getCustomConfig("craftDiamondCooldown"));

        goldCraftLimiter = new UsageLimiter(craftGoldIngotCooldown);
        shieldCraftLimiter = new UsageLimiter(craftShieldCooldown);
        diamondCraftLimiter = new UsageLimiter(craftDiamondCooldown);
    }


}
