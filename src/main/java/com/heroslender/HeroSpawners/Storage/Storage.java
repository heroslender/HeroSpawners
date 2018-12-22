package com.heroslender.herospawners.storage;

import com.heroslender.herospawners.spawner.ISpawner;
import com.heroslender.herospawners.spawner.Spawner;
import org.bukkit.Location;

import java.util.Map;

public interface Storage {

    Map<Location, ISpawner> getSpawners();

    ISpawner getSpawner(Location location);

    void saveSpawner(Location location, int quantidade);

    void saveSpawnerCache(Spawner spawner);

    void delSpawner(Location location);

    void onDisable();
}
