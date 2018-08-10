package com.heroslender.HeroSpawners.Storage;

import com.heroslender.HeroSpawners.HeroSpawners;
import com.heroslender.HeroSpawners.Spawner.Spawner;
import com.heroslender.HeroSpawners.Utils.Utilities;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

public class StorageMySql extends StorageCache {

    private HikariDataSource hikariDataSource;

    public StorageMySql() {
        FileConfiguration config = HeroSpawners.getInstance().getConfig();
        hikariDataSource = new HikariDataSource();
        hikariDataSource.setMaximumPoolSize(10);
        hikariDataSource.setJdbcUrl("jdbc:mysql://" + config.getString("MySql.host", "localhost") + ":" + config.getString("MySql.port", "3306") + "/" + config.getString("MySql.database", "HeroSpawners"));
        hikariDataSource.setUsername(config.getString("MySql.user", "root"));
        hikariDataSource.setPassword(config.getString("MySql.pass", ""));
        hikariDataSource.addDataSourceProperty("autoReconnect", "true");

        createDatabase();

        loadSpawners();
    }

    private void loadSpawners() {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM " + SPAWNERS + ";")) {
                try (ResultSet rs = ps.executeQuery()) {
                    spawners.clear();
                    int failed = 0;
                    while (rs.next()) {
                        Location location = Utilities.str2loc(rs.getString(SPAWNERS_LOC));
                        if (location.getWorld() == null || location.getBlock().getType() != Material.MOB_SPAWNER) {
                            failed++;
                            continue;
                        }
                        spawners.put(location, new Spawner(location, rs.getInt(SPAWNERS_QUANT)));
                    }
                    if (failed != 0)
                        log(Level.WARNING, "Nao foi possivel carregar " + failed + " spawners!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSpawner(Location location, int quantidade) {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + SPAWNERS +
                    " (" + SPAWNERS_LOC + "," + SPAWNERS_QUANT + ") " +
                    "VALUES(?, ?) ON DUPLICATE KEY UPDATE " +
//                    SPAWNERS_LOC + "=" + SPAWNERS_LOC + ", " +
                    SPAWNERS_QUANT + "=?")) {
                ps.setString(1, Utilities.loc2str(location));
                ps.setInt(2, quantidade);
                ps.setInt(3, quantidade);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao guardar o " +
                    "spawner(loc=\"" + Utilities.loc2str(location) + "\", quantidade=\"" + quantidade + "\").");
            e.printStackTrace();
        }
    }

    @Override
    public void delSpawner(Location location) {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + SPAWNERS + " WHERE " + SPAWNERS_LOC + " = ?;")) {
                ps.setString(1, Utilities.loc2str(location));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao apagar o spawner(loc=\"" + Utilities.loc2str(location) + "\").");
            e.printStackTrace();
        }
    }

    private void createDatabase() {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(TABLE_CREATE)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao criar a tabela.");
            e.printStackTrace();
        }
    }

    private void log(Level level, String message) {
        HeroSpawners.getInstance().getLogger().log(level, "[MySql] " + message);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        hikariDataSource.close();
    }
}
