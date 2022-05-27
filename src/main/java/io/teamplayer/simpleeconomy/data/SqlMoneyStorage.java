package io.teamplayer.simpleeconomy.data;

import org.bukkit.OfflinePlayer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Stores and retrieves money from an SQL database
 */
public class SqlMoneyStorage implements MoneyStorage {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DataSource connectionSupplier;

    /**
     * Stores and retrieves money from an SQL database
     * using datasource for connections to database
     *
     * @param connectionPool DataSource for pooled SQL connections to database
     */
    public SqlMoneyStorage(DataSource connectionPool) {
        this.connectionSupplier = connectionPool;
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            double value = 0;

            try (Connection connection = connectionSupplier.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT balance FROM balances WHERE uuid = ?"
                )) {
                    statement.setString(1, player.getUniqueId().toString());

                    ResultSet resultSet = statement.executeQuery();
                    resultSet.next();

                    value = resultSet.getDouble(1);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            return value;
        }, executor);
    }

    @Override
    public void updateBalance(OfflinePlayer player, double newAmount) {
        executor.submit(() -> {
            try (Connection connection = connectionSupplier.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO balances (uuid, balance) " +
                                "VALUES(?, ?) " +
                                "ON DUPLICATE KEY UPDATE " +
                                "    uuid = VALUES(uuid), " +
                                "    balance = VALUES(balance)"
                )) {
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setDouble(2, MoneyStorage.roundDouble(newAmount));

                    statement.executeUpdate();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }
}
