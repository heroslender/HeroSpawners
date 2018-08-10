package com.heroslender.HeroSpawners.events;

import com.heroslender.HeroSpawners.HeroSpawners;
import com.heroslender.HeroSpawners.Spawner.ISpawner;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.*;

public class SpawnerEvent implements Listener {
    private static final Random random = new Random();
    private Map<Location, Long> preventMultiple;

    public SpawnerEvent() {
        preventMultiple = new HashMap<>();
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (e.isCancelled())
            return;

        ISpawner spawner = HeroSpawners.getInstance().getStorage().getSpawner(e.getSpawner().getLocation());
        if (spawner == null) return;

        if (!canSpawn(e.getSpawner().getLocation())){
            e.setCancelled(true);
            return;
        }
        preventMultiple.put(e.getSpawner().getLocation(), System.currentTimeMillis());

        if (HeroSpawners.getInstance().getNewSpawner().remove(e.getSpawner().getLocation()))
            e.getSpawner().setDelay(-1);

        e.setCancelled(HeroSpawners.getInstance().getMobStackerSuport().createOrAddStack(spawner, e.getEntity(),
                (int) Math.round((0.5D + random.nextDouble()) * spawner.getQuatidade())));

    }

    private boolean canSpawn(Location location) {
        return !preventMultiple.containsKey(location) || preventMultiple.get(location) + 250 <= System.currentTimeMillis();
    }
}
