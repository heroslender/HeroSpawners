package com.heroslender.herospawners.storage;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.spawner.Spawner;
import com.heroslender.herospawners.utils.Utilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

public class StorageSqlLite extends StorageCache {

    private SQLiteDataSource dataSource;

    public StorageSqlLite() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + new File(HeroSpawners.getInstance().getDataFolder(), "BaseDeDados.db"));

        createDatabase();

        loadSpawners();
        log(Level.INFO, "Foram carregados " + spawners.size() + " spawners!");
    }

    private void loadSpawners() {
        try (Connection c = dataSource.getConnection()) {
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
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO " + SPAWNERS + " (" +
                    SPAWNERS_LOC + "," + SPAWNERS_QUANT + ") " +
                    "VALUES(?, ?)")) {
                ps.setString(1, Utilities.loc2str(location));
                ps.setInt(2, quantidade);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao guardar o " +
                    "spawner(loc=\"" + Utilities.loc2str(location) + "\", quantidade=\"" + quantidade + "\").", e);
        }
    }

    @Override
    public void delSpawner(Location location) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + SPAWNERS + " WHERE " + SPAWNERS_LOC + " = ?;")) {
                ps.setString(1, Utilities.loc2str(location));
                ps.executeUpdate();
                spawners.remove(location);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "Ocurreu um erro ao apagar o spawner(loc=\"" + Utilities.loc2str(location) + "\").", e);
        }
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
