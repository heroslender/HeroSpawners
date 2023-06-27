package com.heroslender.herospawners.service.storage;

import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.service.Service;
import org.bukkit.Location;

import java.util.Map;

public interface StorageService extends Service {

    Map<Location, ISpawner> getSpawners();

    Map<Location, ISpawner> getSpawners(String world);

    void save(final ISpawner spawner);

    void update(final ISpawner spawner);

    void delete(final ISpawner spawner);
}
