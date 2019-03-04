package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.utils.Utilities;
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

        if (HeroSpawners.getInstance().getNewSpawner().remove(location))
            e.getSpawner().setDelay(-1);

        e.setCancelled(HeroSpawners.getInstance().getMobStackerSuport().createOrAddStack(spawner, e.getEntity(),
                (int) Math.round((0.5D + Utilities.getRandom().nextDouble()) * spawner.getAmount())));

    }
}
