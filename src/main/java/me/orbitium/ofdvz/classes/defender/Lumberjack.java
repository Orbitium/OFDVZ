package me.orbitium.ofdvz.classes.defender;

import me.orbitium.ofdvz.OFDVZ;
import me.orbitium.ofdvz.classes.root.ClassType;
import me.orbitium.ofdvz.classes.root.OFClass;
import me.orbitium.ofdvz.util.ItemBuilder;
import me.orbitium.ofdvz.util.ItemManager;
import me.orbitium.ofdvz.util.MessageUtil;
import me.orbitium.ofdvz.util.UsageLimiter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Lumberjack extends OFClass {

    UsageLimiter skillLimiter;
    UsageLimiter plankLimiter;
    UsageLimiter bowlLimiter;
    public static UsageLimiter mortarLimiter;
    int plankAmount = 0;
    int bowlAmount = 0;
    public static int bowlPerMortar = 0;
    ItemStack bowl = new ItemStack(Material.BOWL);
    public static ItemStack mortar;
    public static NamespacedKey key;
    public static Material newMaterial;

    public static int range = 0;
    int maxTree = 0;
    TreeType type;

    public Lumberjack() {
        super("Lumberjack", ClassType.Defender);

        key = new NamespacedKey(OFDVZ.getInstance(), "mortar");

        configCommend("# The cooldown of summon tree skill");
        registerNewConfig("skillCooldown", "3");

        configCommend("# The cooldown of create planks (AKA break tree)");
        registerNewConfig("createPlanksCooldown", "3");

        configCommend("# The cooldown of craft bowls");
        registerNewConfig("createBowlCooldown", "3");

        configCommend("# The cooldown of craft mortar");
        registerNewConfig("createMortarCooldown", "10");

        configCommend("# How much bowl will be used for single mortar");
        registerNewConfig("bowlPerMortar", "10");

        configCommend("# How much planks will be generated per tree ");
        registerNewConfig("plankPerTree", "4");

        configCommend("# How much planks will be used for single bowl");
        registerNewConfig("plankPerBowl", "4");

        configCommend("# Range for create tree using skill. That config determines ONE DIRECTION, If you say '2' the plugin will scan for 4x4 area");
        registerNewConfig("skillRange", "3");

        configCommend("# How much tree can be generated per skill usage");
        registerNewConfig("maxTreeGenerate", "4");

        configCommend("# What's type of tree will be generated using skill");
        configCommend("# Please check that document https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/TreeType.html");
        configCommend("# Use ONLY documented tree types. Like TREE (it's oak tree) or ACACIA_TREE");
        registerNewConfig("treeType", "TREE");

        configCommend("# What's the item type of mortar");
        registerNewConfig("mortarType", "MILK_BUCKET");

        configCommend("# What's the item name of mortar");
        registerNewConfig("mortarName", "&2Mortar");

        configCommend("# If a cobblestone in range, what will it's type");
        registerNewConfig("newCobblestoneType", "BRICKS");

        registerNewEvent(PlayerInteractEvent.class, (e) -> {
            PlayerInteractEvent event = (PlayerInteractEvent) e;
            if (event.getClickedBlock() == null || event.getHand() != EquipmentSlot.HAND)
                return;

            if (event.getClickedBlock().getType().equals(Material.STONECUTTER))
                if (event.getPlayer().getInventory().getItemInMainHand().getType().name().contains("_PLANKS"))
                    ItemManager.convertItem(event.getPlayer(), bowlAmount, bowl, bowlLimiter, "Bowl", bowlAmount + "x Plank");

            if (!event.getPlayer().getInventory().getItemInMainHand().getType().name().contains("AXE"))
                return;

            Material material = event.getClickedBlock().getType();

            if (material.name().contains("LOG")) {
                if (plankLimiter.check(event.getPlayer())) {
                    event.getClickedBlock().setType(Material.AIR);
                    String[] q = material.name().split("_");
                    Material material1 = null;
                    if (q.length == 2)
                        material1 = Material.getMaterial(q[0] + "_PLANKS");
                    if (q.length == 3)
                        material1 = Material.getMaterial(q[1] + "_PLANKS");
                    event.getPlayer().getInventory().addItem(new ItemStack(material1, plankAmount));
                    plankLimiter.update(event.getPlayer());
                }
            }
        });

        registerNewEvent(PlayerSwapHandItemsEvent.class, (e) -> {
            PlayerSwapHandItemsEvent event = (PlayerSwapHandItemsEvent) e;
            if (event.getOffHandItem() == null)
                return;
            if (!event.getOffHandItem().getType().name().contains("AXE"))
                return;

            event.setCancelled(true);

            if (!skillLimiter.check(event.getPlayer()))
                return;

            List<Block> possibleSpawns = new ArrayList<>();

            Player player = event.getPlayer();
            World world = player.getWorld();

            Location min = player.getLocation().add(-range, 0, -range);
            Location max = player.getLocation().add(range, 0, range);

            BoundingBox bb = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());

            for (double x = bb.getMinX(); x != bb.getMinX() + (range * 2) - 1; x++)
                for (double z = bb.getMinZ(); z != bb.getMinZ() + (range * 2) - 1; z++) {
                    Block block = world.getHighestBlockAt((int) x, (int) z);
                    if (block.getType().equals(Material.DIRT) || block.getType().equals(Material.GRASS_BLOCK)) {
                        possibleSpawns.add(block);
                    }
                }

            possibleSpawns.remove(player.getLocation().add(0, -1, 0).getBlock());

            if (possibleSpawns.size() == 0) {
                MessageUtil.sendMessageToPlayer(player, "messages.error.thereIsNotPossibleLocationsForGenerateTree");
                return;
            }

            int counter = 0;
            while (counter < maxTree + 1) {
                if (possibleSpawns.size() - 1 == 0)
                    break;
                Block selectedBlock = possibleSpawns.get(OFDVZ.random.nextInt(possibleSpawns.size() - 1));
                if (world.generateTree(selectedBlock.getLocation().add(0, 1, 0), TreeType.TREE))
                    counter++;
                possibleSpawns.remove(selectedBlock);
            }
            skillLimiter.update(player);
        });

        loadConfig();
        plankLimiter = new UsageLimiter(Integer.parseInt(getCustomConfig("createPlanksCooldown")));
        bowlLimiter = new UsageLimiter(Integer.parseInt(getCustomConfig("createBowlCooldown")));
        skillLimiter = new UsageLimiter(Integer.parseInt(getCustomConfig("skillCooldown")));
        mortarLimiter = new UsageLimiter(Integer.parseInt(getCustomConfig("createMortarCooldown")));
        plankAmount = Integer.parseInt(getCustomConfig("plankPerTree"));
        bowlAmount = Integer.parseInt(getCustomConfig("plankPerBowl"));
        bowlPerMortar = Integer.parseInt(getCustomConfig("bowlPerMortar"));

        range = Integer.parseInt(getCustomConfig("skillRange"));
        maxTree = Integer.parseInt(getCustomConfig("maxTreeGenerate"));
        type = TreeType.valueOf(getCustomConfig("treeType"));

        String name = ChatColor.translateAlternateColorCodes('&', getCustomConfig("mortarName"));
        Material material = Material.getMaterial(getCustomConfig("mortarType").toUpperCase(Locale.US));
        mortar = new ItemBuilder(material, name).build(true);

        ItemMeta itemMeta = mortar.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
        mortar.setItemMeta(itemMeta);

        newMaterial = Material.getMaterial(getCustomConfig("newCobblestoneType"));
    }

}
