package com.heroslender.HeroSpawners.Storage;

import com.heroslender.HeroSpawners.Spawner.ISpawner;
import com.heroslender.HeroSpawners.Spawner.Spawner;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class StorageCache implements Storage {
    static final String SPAWNERS = "spawners";
    static final String SPAWNERS_LOC = "location";
    static final String SPAWNERS_QUANT = "quantidade";
    public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + SPAWNERS + " (" +
            "`" + SPAWNERS_LOC + "` varchar(128) NOT NULL," +
            "`" + SPAWNERS_QUANT + "` varchar(32) NOT NULL," +
            "PRIMARY KEY (`" + SPAWNERS_LOC + "`)" +
            ");";

    final Map<Location, ISpawner> spawners;

    StorageCache() {
        spawners = new ConcurrentHashMap<>();
    }

    @Override
    public void saveSpawnerCache(Spawner spawner) {
        spawners.put(spawner.getSpawnerLocation(), spawner);
    }

    @Override
    public ISpawner getSpawner(Location location) {
        if (spawners.containsKey(location)) return spawners.get(location);
        return null;
    }

    @Override
    public void onDisable() {
        spawners.forEach((location, spawner) -> ((Spawner) spawner).unload());
        spawners.clear();
    }

    @Override
    public Map<Location, ISpawner> getSpawners() {
        return spawners;
    }
}
