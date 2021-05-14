package io.teamplayer.simpleeconomy;


import io.teamplayer.simpleeconomy.command.BalanceCommand;
import io.teamplayer.simpleeconomy.command.SetBalanceCommand;
import io.teamplayer.simpleeconomy.data.LocalMoneyStorage;
import io.teamplayer.simpleeconomy.data.MoneyStorage;
import io.teamplayer.simpleeconomy.data.SqlMoneyStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.sql.SQLException;

public class SimpleEconomy extends JavaPlugin {

    private static SimpleEconomy instance;

    private MoneyStorage moneyStorage;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Bukkit.getPluginCommand("balance").setExecutor(new BalanceCommand());
        Bukkit.getPluginCommand("setbalance").setExecutor(new SetBalanceCommand());

        //Initialize the external money storage
        DataSource externalConnectionPool = null;

        try {
            externalConnectionPool = getConnectionPool(getConfig().getConfigurationSection("database"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if (externalConnectionPool != null) {
            moneyStorage = new LocalMoneyStorage(this, new SqlMoneyStorage(externalConnectionPool));
        } else {
            Bukkit.getLogger().info("Unable to connect to database. All balances will only be in memory");
            moneyStorage = new LocalMoneyStorage(this);
        }

    }

    private DataSource getConnectionPool(ConfigurationSection yamlConfig) throws SQLException {
        String host, username, password, database;
        int port;

        host = yamlConfig.getString("host");
        username = yamlConfig.getString("username");
        password = yamlConfig.getString("password");
        port = yamlConfig.getInt("port");
        database = yamlConfig.getString("database");

        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false", host, port, database);

        HikariConfig databaseConfig = new HikariConfig();

        databaseConfig.setJdbcUrl(url);
        databaseConfig.setUsername(username);
        databaseConfig.setPassword(password);

        return new HikariDataSource(databaseConfig);
    }

    public MoneyStorage getMoneyStorage() {
        return moneyStorage;
    }

    public static SimpleEconomy getInstance() {
        return instance;
    }
}
