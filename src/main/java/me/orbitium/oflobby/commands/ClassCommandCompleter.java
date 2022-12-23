package me.orbitium.oflobby.commands;

import me.orbitium.oflobby.classes.root.ClassSelect;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassCommandCompleter implements TabCompleter {

    List arrayList = new ArrayList();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2)
            return Arrays.asList("dwarves", "heroes", "zombies", "villains");
        else if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach((player) -> {
                playerNames.add(player.getName());
            });
            return playerNames;
        } else if (args.length == 3) {
            if (args[1].equals("dwarves"))
                return Arrays.asList("alchemist", "baker", "blacksmith", "builder", "lumberjack");
            if (args[1].equals("zombies"))
                return Arrays.asList("creeper", "silverfish", "skeleton", "spider", "zombie");
            if (args[1].equals("heroes"))
                return Arrays.asList("Zachattck", "Voidwalker sea7", "Tehlone Tiger", "Kiki The Paladin",
                        "BANG The Sorcerer", "Koko", "Brood Mother");
            if (args[1].equals("villains"))
                return Collections.singletonList("broodmother");
        }
        return arrayList;
    }
}
