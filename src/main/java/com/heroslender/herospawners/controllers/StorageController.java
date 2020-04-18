package com.heroslender.herospawners.controllers;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.services.StorageService;
import com.heroslender.herospawners.models.ISpawner;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;

@RequiredArgsConstructor
public class StorageController implements Controller {
    // Delay entre updates na base de dados, 5 minutos
    private static final long UPDATE_DELAY = 20 * 60 * 5;
    private final StorageService storageService;
    private final Executor executor;
    private final Map<Location, ISpawner> cachedSpawners = new ConcurrentHashMap<>();

    @Override
    public void init() {
        storageService.init();
        cachedSpawners.clear();

        Map<Location, ISpawner> spawners = storageService.getSpawners();
        HeroSpawners.getInstance().getLogger().log(Level.INFO, "[Storage] Foram carregados {0} spawners da base de dados.", spawners.size());

        Bukkit.getScheduler().runTaskTimerAsynchronously(HeroSpawners.getInstance(), this::save, UPDATE_DELAY, UPDATE_DELAY);

        cachedSpawners.putAll(spawners);
    }

    public void save() {
        executor.execute(() -> {
            for (ISpawner spawner : cachedSpawners.values()) {
                if (((Spawner) spawner).isUpdateRequired()) {
                    updateSpawner(spawner);
                }
            }
        });
    }

    public void saveSpawner(ISpawner spawner) {
        cachedSpawners.put(spawner.getLocation(), spawner);
        executor.execute(() -> storageService.save(spawner));
    }

    public void updateSpawner(final ISpawner spawner) {
        storageService.update(spawner);
        ((Spawner) spawner).setUpdateRequired(false);
    }

    public void deleteSpawner(final ISpawner spawner) {
        cachedSpawners.remove(spawner.getLocation());
        executor.execute(() -> storageService.delete(spawner));
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
