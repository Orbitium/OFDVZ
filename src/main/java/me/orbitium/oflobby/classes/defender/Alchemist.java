package me.orbitium.oflobby.classes.defender;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.*;

public class Alchemist extends OFClass {

    public static final NamespacedKey drinkRemainKey = new NamespacedKey(OFDVZ.getInstance(), "remainAle");

    public static final Map<Player, Integer> drinkCooldownCounter = new HashMap<>();
    private final ItemStack bottleGenerator;
    private final ItemStack emptyBottle = new ItemStack(Material.GLASS_BOTTLE);
    private final String loreOfBottleGenerator;
    public static ItemStack ale;
    private static int aleRegen;
    public static int aleUseLimit;

    int brewCooldown = -1;
    public static int drinkCooldown = -1;
    int bottleGenerateCooldown = -1;
    int neededSeed = -1;

    int counter = 0;

    UsageLimiter limiter;

    public Alchemist() {
        super("Alchemist", ClassType.Defender);

        configCommend("# The cooldown of brewing new ale for alchemists");
        registerNewConfig("cooldownPerBrew", "10");

        configCommend("# The cooldown of drink ale for all classes");
        registerNewConfig("cooldownPerDrink", "10");

        configCommend("# The cooldown of generate empty bottle");
        registerNewConfig("cooldownPerBottleGenerate", "10");

        configCommend("# Set \"Bottle generator\" item properties");
        configCommend("# The name of the item");
        registerNewConfig("bottleGeneratorName", "&2Bottle &4Generator\n");
        configCommend("# The type of the item");
        registerNewConfig("bottleGeneratorItemType", "STONE\n");
        configCommend("# If that line is empty, item lore won't appear. [time] will be replaced from plugin as a remain cooldown time");
        registerNewConfig("bottleGeneratorLore", "New bottle will be created before [time] second");

        configCommend("# Set \"Dwarf Ale\" item properties");
        configCommend("# The name of the item");
        registerNewConfig("aleName", "&aAle");
        configCommend("# The type of the item");
        registerNewConfig("aleItemType", "default");
        configCommend("# How much seed needed per ale");
        registerNewConfig("neededSeed", "3");
        configCommend("# If line is empty, item lore won't appear. [cooldown] will be replaced from plugin as a remain cooldown time");
        registerNewConfig("aleLore", "Gives +[health_amount] health. [cooldown] second cooldown.");
        registerNewConfig("aleRemainLore", "Remain usage [remain_usage].");
        configCommend("# How much health will give ale to player");
        registerNewConfig("aleRegen", "5");
        configCommend("# An ale how much time be used");
        registerNewConfig("aleUseLimit", "3");

        registerNewEvent(PlayerInteractEvent.class, (e) -> {
            PlayerInteractEvent event = (PlayerInteractEvent) e;
            if (event.getHand() != EquipmentSlot.HAND)
                return;
            if (event.getClickedBlock() == null || event.getAction() != Action.LEFT_CLICK_BLOCK)
                return;
            if (event.getClickedBlock().getType().equals(Material.BREWING_STAND)) {
                Player player = event.getPlayer();
                if (player.getInventory().getItemInMainHand().getType() != Material.POTION)
                    return;
                PotionMeta potionEffect = (PotionMeta) player.getInventory().getItemInMainHand().getItemMeta();
                if (potionEffect.getBasePotionData().getType() != PotionType.WATER)
                    return;

                Location location = event.getClickedBlock().getLocation();

                int g = 0;
                for (ItemStack content : player.getInventory().getContents()) {
                    if (content == null)
                        continue;
                    if (content.getType().equals(Material.WHEAT_SEEDS)) {
                        if (g + content.getAmount() >= neededSeed) {
                            if (ItemManager.convertItem(player, 1, ale, limiter, "Ale", "1 x Water bottle")) {
                                ParticleUtil.spawnGreenParticle(player, event.getClickedBlock().getLocation());
                                content.setAmount(Math.max(0, content.getAmount() - neededSeed));
                                if (g - neededSeed > 0)
                                    player.getInventory().addItem(new ItemStack(Material.WHEAT_SEEDS, g - neededSeed));
                                ParticleUtil.spawnGreenParticle(player, location.add(0, 1, 0));
                                player.getInventory().addItem(new ItemStack(Material.WHEAT_SEEDS, g));
                                return;
                            } else {
                                ParticleUtil.spawnRedParticle(player, location.add(0, 1, 0));
                                return;
                            }
                        } else {
                            g += content.getAmount();
                            content.setAmount(0);
                        }
                    }
                }
                ParticleUtil.spawnRedParticle(player, location.add(0, 1, 0));
                MessageUtil.sendMessageToPlayer(player, "messages.error.notEnoughResource", "[source_name]", neededSeed + "x Seed");
            }
        });
        loadConfig();
        brewCooldown = Integer.parseInt(getCustomConfig("cooldownPerBrew").replaceAll(" ", ""));
        aleRegen = Integer.parseInt(getCustomConfig("aleRegen").replaceAll(" ", ""));
        drinkCooldown = Integer.parseInt(getCustomConfig("cooldownPerDrink").replaceAll(" ", ""));
        bottleGenerateCooldown = Integer.parseInt(getCustomConfig("cooldownPerBottleGenerate").replaceAll(" ", ""));
        aleUseLimit = Integer.parseInt(getCustomConfig("aleUseLimit").replaceAll(" ", ""));
        neededSeed = Integer.parseInt(getCustomConfig("neededSeed"));

        ItemBuilder builder = new ItemBuilder(Material.getMaterial(getCustomConfig("bottleGeneratorItemType").replaceAll(" ", "")));
        builder.setName(ChatColor.translateAlternateColorCodes('&', getCustomConfig("bottleGeneratorName", false)));
        builder.setLore(ChatColor.translateAlternateColorCodes('&', getCustomConfig("bottleGeneratorLore", false)));
        loreOfBottleGenerator = getCustomConfig("bottleGeneratorLore", false);
        bottleGenerator = builder.build(true);
        addStarterItem(bottleGenerator);

        if (getCustomConfig("aleItemType").replaceAll(" ", "").equals("default")) {
            ItemStack itemStack = new ItemStack(Material.POTION);
            PotionMeta pm = (PotionMeta) itemStack.getItemMeta();
            pm.setBasePotionData(new PotionData(PotionType.LUCK));
            ale = itemStack;
        }
        ItemBuilder builder2;
        if (ale == null)
            builder2 = new ItemBuilder(Material.getMaterial(getCustomConfig("aleItemType").replaceAll(" ", "")));
        else
            builder2 = new ItemBuilder(ale);
        builder2.setName(ChatColor.translateAlternateColorCodes('&', getCustomConfig("aleName")));
        String lore = getCustomConfig("aleLore", false).replace("[health_amount]", getCustomConfig("aleRegen"));
        lore = lore.replace("[cooldown]", drinkCooldown + "");

        List<String> loreList = new ArrayList<>();
        loreList.add(ChatColor.translateAlternateColorCodes('&', lore));

        if (!getCustomConfig("aleRemainLore").isBlank()) {
            lore = getCustomConfig("aleRemainLore", false).replace("[remain_usage]", aleUseLimit + "");
            loreList.add(ChatColor.translateAlternateColorCodes('&', lore));
        }

        builder2.setLore(loreList);
        ale = builder2.build(true);

        ItemMeta aleMeta = ale.getItemMeta();
        PersistentDataContainer pdc = aleMeta.getPersistentDataContainer();
        pdc.set(drinkRemainKey, PersistentDataType.INTEGER, aleUseLimit);
        ale.setItemMeta(aleMeta);


        Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            for (Player registeredPlayer : registeredPlayers) {
                for (ItemStack content : registeredPlayer.getInventory().getContents()) {
                    if (content == null)
                        continue;
                    if (content.getType().equals(bottleGenerator.getType())) {
                        if (++counter >= bottleGenerateCooldown) {
                            registeredPlayer.getInventory().addItem(emptyBottle);
                            counter = 0;
                        }

                        ItemMeta itemMeta = content.getItemMeta();
                        String ll = loreOfBottleGenerator.replace("[time]"
                                , (bottleGenerateCooldown - counter) + "");
                        itemMeta.setLore(Collections.singletonList(ll));
                        content.setItemMeta(itemMeta);
                        break;
                    }
                }
            }
        }, 20L, 20L);
        limiter = new UsageLimiter(brewCooldown);
    }


    public void drink(Player player) {
        player.setHealth(Math.min(player.getHealth() + aleRegen, player.getMaxHealth()));
        MessageUtil.sendMessageToPlayer(player, "messages.aleUsed");

        drinkCooldownCounter.put(player, Time.dateToInt());
        ItemMeta itemMeta = player.getInventory().getItemInMainHand().getItemMeta();

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        int remain = pdc.get(drinkRemainKey, PersistentDataType.INTEGER) - 1;
        if (remain == 0) {
            player.getInventory().getItemInMainHand().setAmount(0);
            return;
        }
        pdc.set(drinkRemainKey, PersistentDataType.INTEGER, remain);

        String lore = getCustomConfig("aleLore", false).replace("[health_amount]", getCustomConfig("aleRegen"));
        lore = lore.replace("[cooldown]", drinkCooldown + "");

        List<String> loreList = new ArrayList<>();
        loreList.add(ChatColor.translateAlternateColorCodes('&', lore));

        if (!getCustomConfig("aleRemainLore").isBlank()) {
            lore = getCustomConfig("aleRemainLore", false).replace("[remain_usage]", remain + "");
            loreList.add(ChatColor.translateAlternateColorCodes('&', lore));
        }

        itemMeta.setLore(loreList);
        player.getInventory().getItemInMainHand().setItemMeta(itemMeta);
    }
}
