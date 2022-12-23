package me.orbitium.ofdvz;

import me.orbitium.ofdvz.classes.root.ClassManager;
import me.orbitium.ofdvz.classes.root.ClassType;
import me.orbitium.ofdvz.classes.root.OFClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;

public class OFScoreboard {

    public static Scoreboard scoreboard;
    public static String prefix;

    public static void init() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        prefix = ChatColor.translateAlternateColorCodes('&',
                OFDVZ.getInstance().getConfig().getString("ui.nameTagPrefix"));

        for (Map.Entry<ClassType, List<OFClass>> entry : ClassManager.classes.entrySet()) {
            for (OFClass ofClass : entry.getValue()) {
                ofClass.team = scoreboard.registerNewTeam(ofClass.name);
                ofClass.team.setPrefix(prefix.replace("[class_name]",
                        ChatColor.translateAlternateColorCodes('&', ofClass.displayName)) + " ");
                ofClass.team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            }
        }
    }

    public static void registerPlayer(ClassType type, Player player) {
        player.setScoreboard(scoreboard);
        if (!player.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER)) {
            scoreboard.getTeam("defenders").addEntry(player.getName());
            player.setPlayerListName(prefix + player.getName());
        }
        else {
            scoreboard.getTeam("attackers").addEntry(player.getName());
            player.setPlayerListName(prefix + player.getName());
        }
    }

}
