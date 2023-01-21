package me.orbitium.ofdvz.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EventCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1)
            return Collections.singletonList("plague");
        else if (args.length == 2)
            return Collections.singletonList("start");
        else if (args.length == 3)
            return Arrays.asList("25", "50", "75", "100", "<percentage>");
        return new ArrayList<>();
    }
}
