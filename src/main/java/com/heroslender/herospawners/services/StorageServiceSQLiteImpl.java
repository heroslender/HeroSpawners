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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class StorageServiceSQLiteImpl implements StorageServiceSql {

    private SQLiteDataSource dataSource;

    public StorageServiceSQLiteImpl() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + new File(HeroSpawners.getInstance().getDataFolder(), "BaseDeDados.db"));

        createDatabase();
    }

    @Override
    public Map<Location, ISpawner> getSpawners() {
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
            e.printStackTrace();
        }
        return spawners;
    }

    @Override
    public void save(final ISpawner spawner) {
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
            log(Level.SEVERE, "Ocurreu um erro ao guardar o " +
                    "models(loc=\"" + Utilities.loc2str(spawner.getLocation()) + "\", quantidade=\"" + spawner.getAmount() + "\").", e);
        }
    }

    @Override
    public void update(ISpawner spawner) {
        try (Connection c = dataSource.getConnection()) {
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
    public void delete(final ISpawner spawner) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + SPAWNERS + " WHERE " + SPAWNERS_LOC + " = ?;")) {
                ps.setString(1, Utilities.loc2str(spawner.getLocation()));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao apagar o models(loc=\"" + Utilities.loc2str(spawner.getLocation()) + "\").", e);
        }
    }

    @Override
    public void onDisable() {

    }

    private void createDatabase() {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(TABLE_CREATE)) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao criar a tabela.", e);
        }
    }

    private void log(Level level, String message) {
        HeroSpawners.getInstance().getLogger().log(level, "[SqlLite] " + message);
    }

    private void log(Level level, String message, Throwable thrown) {
        HeroSpawners.getInstance().getLogger().log(level, "[SqlLite] " + message, thrown);
    }
}
