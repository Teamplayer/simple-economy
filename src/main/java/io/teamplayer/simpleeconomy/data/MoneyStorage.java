package io.teamplayer.simpleeconomy.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

/**
 * Store and retrieve money for players
 * Supports CompletableFutures for blocking operations
 */
public interface MoneyStorage {

    /**
     * Get the stored balance for the player.
     * NOTE: Assume future will be executed async of tick loop.
     *
     * @param player player's balance to get
     * @return current player balance stored
     */
    CompletableFuture<Double> getBalance(OfflinePlayer player);

    /**
     * Set the player's stored balance to new value
     *
     * @param player player's balance to update
     * @param newAmount the new balance for the player
     */
    void updateBalance(OfflinePlayer player, double newAmount);

    /**
     * Round doubles to 2 decimal places
     */
    static double roundDouble(double value) {
        BigDecimal tempDecimal = new BigDecimal(value);
        tempDecimal = tempDecimal.setScale(2, RoundingMode.HALF_UP);
        return tempDecimal.doubleValue();
    }
}
