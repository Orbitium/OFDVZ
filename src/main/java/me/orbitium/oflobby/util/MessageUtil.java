package me.orbitium.oflobby.util;

import me.orbitium.oflobby.OFDVZ;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    public static void sendMessageToPlayer(Player player, String path) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                OFDVZ.getInstance().getConfig().getString(path)));
    }

    public static void sendMessageToPlayer(CommandSender player, String path) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                OFDVZ.getInstance().getConfig().getString(path)));
    }

    public static void sendMessageToPlayer(Player player, String path, String holder, String parsed) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                OFDVZ.getInstance().getConfig().getString(path).replace(holder, parsed)));
    }

    public static void sendActionBarMessage(Player player, String path) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                ChatColor.translateAlternateColorCodes('&', OFDVZ.getInstance().getConfig().getString(path))
        ));
    }

    public static void sendActionBarMessage(Player player, String path, String holder, String parsed) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                ChatColor.translateAlternateColorCodes('&',
                        OFDVZ.getInstance().getConfig().getString(path).replace(holder, parsed))
        ));
    }


    public static void sendTitle(Player player, String path, String sPath, String holder, String parse) {
        String title = OFDVZ.getInstance().getConfig().getString(path).replace(holder, parse);
        String alt = OFDVZ.getInstance().getConfig().getString(sPath);
        player.sendTitle(title, alt, 1, 4, 1);
    }

    public static void sendTitle(Player player, String path) {
        String title = OFDVZ.getInstance().getConfig().getString(path);
        player.sendTitle(title, "", 2, 6, 2);
    }
}
