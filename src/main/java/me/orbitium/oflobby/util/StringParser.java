package me.orbitium.oflobby.util;

import org.bukkit.ChatColor;

import java.util.List;

public class StringParser {

    public static String parse(String string, String[] replaces, Object... parse) {
        for (int i = 0; i < replaces.length; i++)
            string = string.replace(replaces[i], (String) parse[i]);
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
