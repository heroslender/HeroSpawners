package com.heroslender.herospawners.controllers;

import com.heroslender.herospawners.services.StorageService;
import com.heroslender.herospawners.models.ISpawner;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class StorageController implements Controller {
    private final StorageService storageService;
    private final Map<Location, ISpawner> cachedSpawners = new ConcurrentHashMap<>();

    @Override
    public void init() {
        cachedSpawners.clear();
        cachedSpawners.putAll(storageService.getSpawners().join());
    }

    public void saveSpawner(ISpawner spawner) {
        cachedSpawners.put(spawner.getLocation(), spawner);
        storageService.save(spawner).join();
    }

    public void updateSpawner(final ISpawner spawner) {
        storageService.update(spawner).join();
    }

    public void deleteSpawner(final ISpawner spawner) {
        storageService.delete(spawner).join();
    }

    public ISpawner getSpawner(final Location location) {
        return cachedSpawners.get(location);
    }

    @Override
    public void stop() {
        storageService.onDisable();
        cachedSpawners.clear();
    }
}
