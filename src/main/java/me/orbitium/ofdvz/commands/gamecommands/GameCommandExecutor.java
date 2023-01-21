package me.orbitium.ofdvz.commands;

import me.orbitium.ofdvz.lobby.Lobby;
import me.orbitium.ofdvz.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp())
            return true;

        if (args.length == 1) {
            switch (args[0]) {
                case "prepare" -> {
                    Lobby.prepare();
                    MessageUtil.sendMessageToPlayer((Player) sender, "messages.playersTeleportedToLobby");
                }
                case "start" -> Lobby.start();
                case "finish" -> {
                    if (!Lobby.playing)
                        return true;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        MessageUtil.sendMessageToPlayer(player, "messages.theGameFinishedByModerator");
                    }
                    Lobby.end();
                }
            }
        }

        return true;
    }
}
