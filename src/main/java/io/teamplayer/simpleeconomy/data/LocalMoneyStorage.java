package io.teamplayer.simpleeconomy.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Store and retrieve money in memory
 *
 * Can be backed by an additional MoneyStore, in which case
 * data from the additional MoneyStore will be loaded into memory on
 * player login, and unloaded on player logout
 */
public class LocalMoneyStorage implements MoneyStorage, Listener {

    private final MoneyStorage externalStorage; //if externalStorage is null we will only store locally
    private final Map<OfflinePlayer, Double> playerBalances = new HashMap<>();

    /**
     * Store money exclusively in memory
     *
     * @param plugin plugin to register as listener owner
     */
    public LocalMoneyStorage(JavaPlugin plugin) {
        this(plugin, null);
    }

    /**
     * Cache money storage in memory and load in and out of
     * backing storage on player login and logout
     *
     * @param plugin plugin to register as listener owner
     * @param backingStorage storage to load and unload from on player login and logout
     */
    public LocalMoneyStorage(JavaPlugin plugin, MoneyStorage backingStorage) {
        externalStorage = backingStorage;

        if (backingStorage != null) Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        if (player.isOnline()) return CompletableFuture.completedFuture(playerBalances.getOrDefault(player, 0D));
        return externalStorage != null ? externalStorage.getBalance(player) : CompletableFuture.completedFuture(0D);
    }

    @Override
    public void updateBalance(OfflinePlayer player, double newAmount) {
        newAmount = MoneyStorage.roundDouble(newAmount);

        if (player.isOnline()) {
            playerBalances.put(player, newAmount);
        } else if (externalStorage != null){
            externalStorage.updateBalance(player, newAmount);
        }
    }
    
    private void loadPlayer(Player player) {
        externalStorage.getBalance(player)
                .thenAccept(d -> playerBalances.put(player, d))
                .exceptionally(e -> {
                    Bukkit.getLogger().info("Failed to load external player balance:" + player.getDisplayName());
                    return null;
                });
    }
    
    private void unloadPlayer(Player player) {
        externalStorage.updateBalance(player, playerBalances.get(player));
        playerBalances.remove(player);
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        unloadPlayer(event.getPlayer());
    }
}
