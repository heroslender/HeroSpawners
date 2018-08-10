package com.heroslender.HeroSpawners.Storage;

import com.heroslender.HeroSpawners.Spawner.ISpawner;
import com.heroslender.HeroSpawners.Spawner.Spawner;
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
