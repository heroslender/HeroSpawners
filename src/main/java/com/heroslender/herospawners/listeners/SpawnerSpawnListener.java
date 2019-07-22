package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.api.events.SpawnerSpawnStackEvent;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.HashMap;
import java.util.Map;

public class SpawnerSpawnListener implements Listener {
    private final Map<Location, Long> preventMultiple = new HashMap<>();

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (e.isCancelled())
            return;
        final Location location = e.getSpawner().getLocation();
        ISpawner spawner = HeroSpawners.getInstance().getStorageController().getSpawner(location);
        if (spawner == null) return;

        final Long lastSpawn = preventMultiple.get(location);
        if (lastSpawn != null && lastSpawn + 250 > System.currentTimeMillis()) {
            e.setCancelled(true);
            return;
        }
        preventMultiple.put(location, System.currentTimeMillis());

        int stackSize = (int) Math.round((0.5D + Utilities.getRandom().nextDouble()) * spawner.getAmount());

        SpawnerSpawnStackEvent spawnerSpawnStackEvent = new SpawnerSpawnStackEvent(
                spawner,
                e.getEntityType(),
                stackSize
        );
        Bukkit.getPluginManager().callEvent(spawnerSpawnStackEvent);

        if (!spawnerSpawnStackEvent.isCancelled()) {
            e.setCancelled(HeroSpawners.getInstance().getMobStackerSupport().createOrAddStack(spawner, e.getEntity(),
                    stackSize));
        }
    }
}
