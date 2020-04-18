package com.heroslender.herospawners.services;

import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface StorageService extends Service {

    Map<Location, ISpawner> getSpawners();

    void save(final ISpawner spawner);

    void update(final ISpawner spawner);

    void delete(final ISpawner spawner);
}
