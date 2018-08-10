package com.heroslender.HeroSpawners.Storage;

import com.heroslender.HeroSpawners.HeroSpawners;
import com.heroslender.HeroSpawners.Spawner.Spawner;
import com.heroslender.HeroSpawners.Utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StorageSqlLite extends StorageCache {

    private SQLiteDataSource dataSource;

    public StorageSqlLite() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + new File(HeroSpawners.getInstance().getDataFolder(), "BaseDeDados.db"));

        createDatabase();

        loadSpawners();
    }

    private void loadSpawners() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + SPAWNERS + ";");
            resultSet = preparedStatement.executeQuery();
//            Map<Location, Integer> spawners = new HashMap<>();
            int failed = 0;
            while (resultSet.next()) {
                Location location = Utilities.str2loc(resultSet.getString(SPAWNERS_LOC));
                if (location.getWorld() == null || location.getBlock().getType() != Material.MOB_SPAWNER) {
                    failed++;
                    continue;
                }
                spawners.put(location, new Spawner(location, resultSet.getInt(SPAWNERS_QUANT)));
//                spawners.put(location, resultSet.getInt(SpawnersDatabase.SPAWNERS_QUANT));
//                HeroSpawners.getInstance().util.atualizaHolograma(location, resultSet.getInt(SpawnersDatabase.SPAWNERS_QUANT));
            }
            if (failed != 0)
                Bukkit.getLogger().warning("[HeroSpawners] Failed to load " + failed + " spawners!");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void saveSpawner(Location location, int quantidade) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("INSERT OR REPLACE INTO " + SPAWNERS + " (" +
                    SPAWNERS_LOC + "," +
                    SPAWNERS_QUANT + ") VALUES('" + Utilities.loc2str(location) + "', '" + quantidade + "')");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[HeroSpawners] Ocurreu um erro ao guardar o spawner.");
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void delSpawner(Location location) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement("DELETE FROM " + SPAWNERS + " WHERE " + SPAWNERS_LOC + " = ?;");
            preparedStatement.setString(1, Utilities.loc2str(location));
            preparedStatement.executeUpdate();

            spawners.remove(location);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[HeroSpawners] Ocurreu um erro ao apagar o spawner.");
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void createDatabase() {
        try {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            try {
                connection = dataSource.getConnection();

                preparedStatement = connection.prepareStatement(TABLE_CREATE);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                Bukkit.getLogger().info("[HeroSpawners] SQL - Ocurreu um erro ao criar a tabela.");
            } finally {
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().info("SQLException in Database.java class.");
        }
    }
}
