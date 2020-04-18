package com.heroslender.herospawners.controllers;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.services.StorageService;
import com.heroslender.herospawners.models.ISpawner;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@RequiredArgsConstructor
public class StorageController implements Controller {
    private final StorageService storageService;
    private final Map<Location, ISpawner> cachedSpawners = new ConcurrentHashMap<>();

    @Override
    public void init() {
        storageService.init();
        cachedSpawners.clear();

        Map<Location, ISpawner> spawners = storageService.getSpawners();
        HeroSpawners.getInstance().getLogger().log(Level.INFO, "[Storage] Foram carregados {0} spawners da base de dados.", spawners.size());

        cachedSpawners.putAll(spawners);
    }

    public void saveSpawner(ISpawner spawner) {
        cachedSpawners.put(spawner.getLocation(), spawner);
        storageService.save(spawner).join();
    }

    public void updateSpawner(final ISpawner spawner) {
        storageService.update(spawner).join();
    }

    public void deleteSpawner(final ISpawner spawner) {
        cachedSpawners.remove(spawner.getLocation());
        storageService.delete(spawner).join();
    }

    public ISpawner getSpawner(final Location location) {
        return cachedSpawners.get(location);
    }

    @Override
    public void stop() {
        cachedSpawners.clear();
        storageService.stop();
    }
}
