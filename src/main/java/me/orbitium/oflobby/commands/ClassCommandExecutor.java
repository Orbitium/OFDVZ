package me.orbitium.oflobby.commands;

import me.orbitium.oflobby.classes.root.ClassManager;
import me.orbitium.oflobby.classes.root.ClassSelect;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import me.orbitium.oflobby.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class ClassCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //class <player> <class>

        if (!sender.isOp())
            return true;

        if (args.length < 2)
            return true;

        StringBuilder nameArg = new StringBuilder();

        if (args.length == 3) {
            nameArg = new StringBuilder(args[2]);
        } else if (args.length > 3) {
            for (int i = 2; i < args.length; i++)
                nameArg.append(args[i]).append(" ");
        }
        if (nameArg.charAt(nameArg.length() - 1) == ' ')
            nameArg.deleteCharAt(nameArg.length() - 1);

        String cName = nameArg.toString().toLowerCase(Locale.US);
        OFClass ofClass = ClassManager.getByName(cName);

        if (ofClass == null) {
            MessageUtil.sendMessageToPlayer((Player) sender, "messages.error.classNotFound");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            MessageUtil.sendMessageToPlayer(sender, "messages.error.playerCannotFound");
            return true;
        }

        OFClass playerClass = ClassManager.getClassFromPlayer(player);

        if (playerClass != null)
            playerClass.registeredPlayers.remove(player);

        ofClass.addPlayer(player, true);

        MessageUtil.sendMessageToPlayer(player, "messages.yourClassChangedByModerator", "[class_name]", ofClass.name);
        MessageUtil.sendMessageToPlayer(player, "messages.classChanged");
        for (ItemStack content : player.getInventory().getStorageContents())
            if (content != null)
                content.setAmount(0);

        ofClass.giveItems(player);

        return true;
    }
}
