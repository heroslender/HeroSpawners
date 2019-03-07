package com.heroslender.herospawners.services;

import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface StorageService {

    CompletableFuture<Map<Location, ISpawner>> getSpawners();

    CompletableFuture<Void> save(final ISpawner spawner);

    CompletableFuture<Void> update(final ISpawner spawner);

    CompletableFuture<Void> delete(final ISpawner spawner);

    void onDisable();
}
