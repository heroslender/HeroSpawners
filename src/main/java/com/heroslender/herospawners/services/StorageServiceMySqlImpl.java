package com.heroslender.herospawners.services;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.utils.Utilities;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@RequiredArgsConstructor
public class StorageServiceMySqlImpl implements StorageServiceSql {
    private final HikariDataSource hikariDataSource = new HikariDataSource();

    @Override
    public void init() {
        FileConfiguration config = HeroSpawners.getInstance().getConfig();
        hikariDataSource.setMaximumPoolSize(10);
        hikariDataSource.setJdbcUrl("jdbc:mysql://" + config.getString("MySql.host", "localhost") + ":" + config.getString("MySql.port", "3306") + "/" + config.getString("MySql.database", "herospawners"));
        hikariDataSource.setUsername(config.getString("MySql.user", "root"));
        hikariDataSource.setPassword(config.getString("MySql.pass", ""));
        hikariDataSource.addDataSourceProperty("autoReconnect", "true");

        createDatabase();
        addOwnerColumn();
    }

    @Override
    public synchronized Map<Location, ISpawner> getSpawners() {
        Map<Location, ISpawner> spawners = new HashMap<>();
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM " + SPAWNERS + ";")) {
                try (ResultSet rs = ps.executeQuery()) {
                    int failed = 0;
                    while (rs.next()) {
                        Location location = Utilities.str2loc(rs.getString(SPAWNERS_LOC));
                        if (location.getWorld() == null || location.getBlock().getType() != HeroSpawners.SPAWNER_TYPE) {
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
            log("Ocurreu um erro ao pegar os spawners todos.", e);
        }
        return spawners;
    }

    @Override
    public synchronized Map<Location, ISpawner> getSpawners(String world) {
        Map<Location, ISpawner> spawners = new HashMap<>();
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM " + SPAWNERS + ";")) {
                try (ResultSet rs = ps.executeQuery()) {
                    int failed = 0;
                    while (rs.next()) {
                        String[] split = rs.getString(SPAWNERS_LOC).split("\\|");
                        if (!world.equals(split[0])) {
                            continue;
                        }

                        Location location = Utilities.str2loc(split);
                        if (location.getWorld() == null || location.getBlock().getType() != HeroSpawners.SPAWNER_TYPE) {
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
            log("Ocurreu um erro ao pegar os spawners todos.", e);
        }
        return spawners;
    }

    @Override
    public synchronized void save(final ISpawner spawner) {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + SPAWNERS +
                    " (" + SPAWNERS_OWNER + "," + SPAWNERS_LOC + "," + SPAWNERS_QUANT + ")" +
                    " VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE " + SPAWNERS_QUANT + "=?, " + SPAWNERS_OWNER + "=?")) {
                ps.setString(1, spawner.getOwner());
                ps.setString(2, Utilities.loc2str(spawner.getLocation()));
                ps.setInt(3, spawner.getAmount());
                ps.setInt(4, spawner.getAmount());
                ps.setString(5, spawner.getOwner());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log("Ocurreu um erro ao guardar o " + spawner + ".", e);
        }
    }

    @Override
    public synchronized void update(final ISpawner spawner) {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + SPAWNERS +
                    " SET " + SPAWNERS_QUANT + "=?" +
                    " WHERE " + SPAWNERS_LOC + "=?")) {
                ps.setInt(1, spawner.getAmount());
                ps.setString(2, Utilities.loc2str(spawner.getLocation()));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log("Ocurreu um erro ao atualizar o " + spawner + ".", e);
        }
    }

    @Override
    public synchronized void delete(ISpawner spawner) {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + SPAWNERS + " WHERE " + SPAWNERS_LOC + " = ?;")) {
                ps.setString(1, Utilities.loc2str(spawner.getLocation()));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log("Ocurreu um erro ao apagar o " + spawner + ".", e);
        }
    }

    private synchronized void createDatabase() {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(TABLE_CREATE)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log("Ocurreu um erro ao criar a tabela.", e);
        }
    }

    private synchronized void addOwnerColumn() {
        try (Connection c = hikariDataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(TABLE_ADD_OWNER_COLUMN)) {
                ps.executeUpdate();
            }
        } catch (SQLException ignore) {
            // Column already presend in the database
        } catch (Exception e) {
            log("Ocurreu um erro ao adicionar os novos campos a tabela.", e);
        }
    }

    private void log(Level level, String message) {
        HeroSpawners.getInstance().getLogger().log(level, "[MySql] " + message);
    }

    private void log(String message, Throwable thrown) {
        HeroSpawners.getInstance().getLogger().log(Level.SEVERE, "[MySql] " + message, thrown);
    }

    @Override
    public synchronized void stop() {
        hikariDataSource.close();
    }
}
