package me.orbitium.oflobby.classes.defender;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class Builder extends OFClass {

    private ItemStack cobblestone;
    private int goldenNuggetDropChange;
    int cobblePerGravel;

    public Builder() {
        super("Builder", ClassType.Defender);

        configCommend("# What's the chance of drop golden nugget per gravel break");
        registerNewConfig("goldenNuggetDropChange", "1");

        configCommend("# How much cobblestone will drop for per gravel");
        registerNewConfig("cobblePerGravel", "1");

        registerNewEvent(BlockBreakEvent.class, (e) -> {
            BlockBreakEvent event = (BlockBreakEvent) e;
            Material material = event.getBlock().getType();
            if (material.equals(Material.COBBLESTONE) || material.equals(Material.GRAVEL) || material.equals(Lumberjack.newMaterial)) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), cobblestone);
                    if (goldenNuggetDropChange > OFDVZ.random.nextInt(100) && material.equals(Material.GRAVEL))
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), OFDVZ.goldenNugget);
            }

        });

        loadConfig();
        goldenNuggetDropChange = Integer.parseInt(getCustomConfig("goldenNuggetDropChange"));
        cobblePerGravel = Integer.parseInt(getCustomConfig("cobblePerGravel"));
        cobblestone = new ItemStack(Material.COBBLESTONE, cobblePerGravel);
    }

}
