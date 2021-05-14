package io.teamplayer.simpleeconomy.command;

import io.teamplayer.simpleeconomy.SimpleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You must specify who's balance you want to see.");
            return false;
        }

        final OfflinePlayer targetPlayer;

        if (args.length == 0) {
            targetPlayer = ((Player) sender);
        } else {
            targetPlayer = Bukkit.getOfflinePlayer(args[0]);
        }

        if (!targetPlayer.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before. Their balance cannot be viewed");
            return true;
        }

        SimpleEconomy.getInstance().getMoneyStorage().getBalance(targetPlayer)
                .thenAccept(d -> sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + "'s balance is " + d));

        return true;
    }
}
