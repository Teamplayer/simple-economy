package io.teamplayer.simpleeconomy.command;

import io.teamplayer.simpleeconomy.SimpleEconomy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetBalanceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) return false;
        if (!StringUtils.isNumeric(args[1])) {
            sender.sendMessage(ChatColor.RED + String.format("'%s' is not numeric.", args[1]));
            return false;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);

        if (!targetPlayer.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before. Their balance cannot be updated.");
            return true;
        }

        SimpleEconomy.getInstance().getMoneyStorage().updateBalance(targetPlayer, Double.parseDouble(args[1]));
        sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "'s new balance is " + args[1]);

        return true;
    }
}
