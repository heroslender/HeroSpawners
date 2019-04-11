package com.heroslender.herospawners.services;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.utils.Utilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class StorageServiceSQLiteImpl implements StorageServiceSql {
    private final SQLiteDataSource dataSource;

    public StorageServiceSQLiteImpl() {
        dataSource = new SQLiteDataSource();

        dataSource.setUrl("jdbc:sqlite:" + new File(HeroSpawners.getInstance().getDataFolder(), "BaseDeDados.db"));
    }

    @Override
    public void init() {
        createDatabase();
        addOwnerColumn();
    }

    @Override
    public CompletableFuture<Map<Location, ISpawner>> getSpawners() {
        return CompletableFuture.supplyAsync(() -> {
            Map<Location, ISpawner> spawners = new HashMap<>();
            try (Connection c = dataSource.getConnection()) {
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

                        if (failed != 0) {
                            log(Level.WARNING, "Nao foi possivel carregar " + failed + " spawners!");
                        }
                    }
                }
            } catch (Exception e) {
                log("Ocurreu um erro ao pegar os spawners todos.", e);
            }
            return spawners;
        }, HeroSpawners.getInstance().getExecutor());
    }

    @Override
    public CompletableFuture<Void> save(final ISpawner spawner) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = dataSource.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + SPAWNERS +
                        " (" + SPAWNERS_OWNER + "," + SPAWNERS_LOC + "," + SPAWNERS_QUANT + ") " +
                        "VALUES(?, ?, ?)")) {
                    ps.setString(1, spawner.getOwner());
                    ps.setString(2, Utilities.loc2str(spawner.getLocation()));
                    ps.setInt(3, spawner.getAmount());
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                log("Ocurreu um erro ao guardar o " + spawner + ".", e);
            }
        }, HeroSpawners.getInstance().getExecutor());
    }

    @Override
    public CompletableFuture<Void> update(ISpawner spawner) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = dataSource.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement("UPDATE " + SPAWNERS +
                        " SET " + SPAWNERS_QUANT + "=?" +
                        " WHERE " + SPAWNERS_LOC + "=?")) {
                    ps.setInt(1, spawner.getAmount());
                    ps.setString(2, Utilities.loc2str(spawner.getLocation()));
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                log("Ocurreu um erro ao guardar o " + spawner + ".", e);
            }
        }, HeroSpawners.getInstance().getExecutor());
    }

    @Override
    public CompletableFuture<Void> delete(final ISpawner spawner) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = dataSource.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + SPAWNERS + " WHERE " + SPAWNERS_LOC + " = ?;")) {
                    ps.setString(1, Utilities.loc2str(spawner.getLocation()));
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                log("Ocurreu um erro ao apagar o " + spawner + ".", e);
            }
        }, HeroSpawners.getInstance().getExecutor());
    }

    @Override
    public void stop() {

    }

    private void createDatabase() {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(TABLE_CREATE)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log("Ocurreu um erro ao criar a tabela.", e);
        }
    }

    private void addOwnerColumn() {
        try (Connection c = dataSource.getConnection()) {
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
        HeroSpawners.getInstance().getLogger().log(level, "[SqlLite] " + message);
    }

    private void log(String message, Throwable thrown) {
        HeroSpawners.getInstance().getLogger().log(Level.SEVERE, "[SqlLite] " + message, thrown);
    }
}
