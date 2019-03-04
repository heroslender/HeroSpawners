package com.heroslender.herospawners.services;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.utils.Utilities;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class StorageServiceMySqlImpl implements StorageServiceSql {

    private final HikariDataSource hikariDataSource;

    public StorageServiceMySqlImpl() {
        FileConfiguration config = HeroSpawners.getInstance().getConfig();
        hikariDataSource = new HikariDataSource();
        hikariDataSource.setMaximumPoolSize(10);
        hikariDataSource.setJdbcUrl("jdbc:mysql://" + config.getString("MySql.host", "localhost") + ":" + config.getString("MySql.port", "3306") + "/" + config.getString("MySql.database", "herospawners"));
        hikariDataSource.setUsername(config.getString("MySql.user", "root"));
        hikariDataSource.setPassword(config.getString("MySql.pass", ""));
        hikariDataSource.addDataSourceProperty("autoReconnect", "true");

        createDatabase();
    }

    @Override
    public Map<Location, ISpawner> getSpawners() {
        Map<Location, ISpawner> spawners = new HashMap<>();
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM " + SPAWNERS + ";")) {
                try (ResultSet rs = ps.executeQuery()) {
                    int failed = 0;
                    while (rs.next()) {
                        Location location = Utilities.str2loc(rs.getString(SPAWNERS_LOC));
                        if (location.getWorld() == null || location.getBlock().getType() != Material.MOB_SPAWNER) {
                            failed++;
                            continue;
                        }
                        spawners.put(location, new Spawner(rs.getString(SPAWNERS_OWNER), location, rs.getInt(SPAWNERS_QUANT)));
                    }
                    if (failed != 0)
                        log(Level.WARNING, "Nao foi possivel carregar " + failed + " spawners!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return spawners;
    }

    @Override
    public void save(final ISpawner spawner) {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + SPAWNERS +
                    " (" + SPAWNERS_OWNER + ","  + SPAWNERS_LOC + "," + SPAWNERS_QUANT + ")" +
                    " VALUES(?, ?, ?)")) {
                ps.setString(1, spawner.getOwner());
                ps.setString(2, Utilities.loc2str(spawner.getLocation()));
                ps.setInt(3, spawner.getAmount());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao guardar o " +
                    "models(loc=\"" + Utilities.loc2str(spawner.getLocation()) + "\", quantidade=\"" + spawner.getAmount() + "\").", e);
        }
    }

    @Override
    public void update(final ISpawner spawner) {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + SPAWNERS +
                    " SET " + SPAWNERS_QUANT + "=?" +
                    " WHERE " + SPAWNERS_LOC + "=?")) {
                ps.setInt(1, spawner.getAmount());
                ps.setString(2, Utilities.loc2str(spawner.getLocation()));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao guardar o " +
                    "models(loc=\"" + Utilities.loc2str(spawner.getLocation()) + "\", quantidade=\"" + spawner.getAmount() + "\").", e);
        }
    }

    @Override
    public void delete(ISpawner spawner) {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + SPAWNERS + " WHERE " + SPAWNERS_LOC + " = ?;")) {
                ps.setString(1, Utilities.loc2str(spawner.getLocation()));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao apagar o models(loc=\"" + Utilities.loc2str(spawner.getLocation()) + "\").", e);
        }
    }

    private void createDatabase() {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(TABLE_CREATE)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao criar a tabela.", e);
        }
    }

    private void log(Level level, String message) {
        HeroSpawners.getInstance().getLogger().log(level, "[MySql] " + message);
    }

    private void log(Level level, String message, Throwable thrown) {
        HeroSpawners.getInstance().getLogger().log(level, "[MySql] " + message, thrown);
    }

    @Override
    public void onDisable() {
        hikariDataSource.close();
    }
}