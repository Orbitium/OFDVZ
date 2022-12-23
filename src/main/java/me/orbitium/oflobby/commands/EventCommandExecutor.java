package me.orbitium.oflobby.commands;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.event.Events;
import me.orbitium.oflobby.lobby.Lobby;
import me.orbitium.oflobby.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp())
            return true;

        // /event plague start <int>

        if (args.length < 2)
            return true;

        if (!args[0].equals("plague") && !args[1].equals("start"))
            return true;

        if (!Lobby.playing) {
            sender.sendMessage(ChatColor.RED + "You can't use that command before game start!");
            return true;
        }

        int a = OFDVZ.getInstance().getConfig().getInt("events.plague.convertPercentage");
        try {
            if (args.length == 3)
                a = Integer.parseInt(args[2]);
        } catch (Exception exception) {
            a = OFDVZ.getInstance().getConfig().getInt("events.plague.convertPercentage");
        }

        if (Events.isPlagueStarted)
            return true;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            MessageUtil.sendMessageToPlayer(onlinePlayer, "events.plague.announcementStart", "[second]",
                    30 + "");
        }
        Events.doNight();

        for (int i = 3; i > 0; i--) {
            int finalI = i;
            if (!Events.isPlagueStarted)
                Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        MessageUtil.sendMessageToPlayer(onlinePlayer, "events.plague.announcementStart", "[second]",
                                finalI + "");
                    }
                }, (30 - i) * 20);
        }

        int finalA = a;
        Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(),
                () -> Events.startStartPlague(finalA), 32 * 20);

        return true;
    }
}
