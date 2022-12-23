package me.orbitium.oflobby;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.List;

public class CropManager {

    public static final List<Block> crops = new ArrayList<>();
    private static final List<Block> queue = new ArrayList<>();
    private static final List<Block> finished = new ArrayList<>();

    public static void tick() {
        for (Block crop : crops) {
            BlockData blockData = crop.getBlockData();
            if (blockData instanceof Ageable) {
                Ageable ageable = (Ageable) blockData;
                ageable.setAge(Math.min(ageable.getAge() + 1, ageable.getMaximumAge()));
                crop.setBlockData(blockData);
                crop.getState().update(true);
                if (ageable.getAge() == ageable.getMaximumAge())
                    finished.add(crop);
            } else
                finished.add(crop);
        }
        crops.removeAll(finished);
        finished.clear();
        crops.addAll(queue);
        queue.clear();
    }

    public static void addQueue(Block block) {
        queue.add(block);
    }
}
