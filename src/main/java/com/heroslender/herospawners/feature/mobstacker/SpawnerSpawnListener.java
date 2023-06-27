package com.heroslender.herospawners.feature.mobstacker;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.api.events.SpawnerSpawnStackEvent;
import com.heroslender.herospawners.feature.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.utils.Utilities;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.Map;
import java.util.WeakHashMap;

@RequiredArgsConstructor
public class SpawnerSpawnListener implements Listener {
    private final MobStackerStrategy mobStackerStrategy;

    private final Map<Location, Long> preventMultiple = new WeakHashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        final Location location = e.getSpawner().getLocation();
        ISpawner spawner = HeroSpawners.getInstance().getSpawnerService().getSpawner(location);
        if (spawner == null) return;

        final Long lastSpawn = preventMultiple.get(location);
        if (lastSpawn != null && lastSpawn + 250 > System.currentTimeMillis()) {
            e.setCancelled(true);
            return;
        }
        preventMultiple.put(location, System.currentTimeMillis());

        final int stackSize = HeroSpawners.getInstance().getConfigurationController().isSpawnRandom() ?
            (int) Math.round((0.5D + Utilities.getRandom().nextDouble()) * spawner.getAmount())
            : spawner.getAmount();

        SpawnerSpawnStackEvent spawnerSpawnStackEvent = new SpawnerSpawnStackEvent(
            spawner,
            e.getEntity(),
            stackSize
        );
        Bukkit.getPluginManager().callEvent(spawnerSpawnStackEvent);

        if (!spawnerSpawnStackEvent.isCancelled()) {
            final boolean willCancel = mobStackerStrategy.createOrAddStack(
                spawner,
                e.getEntity(),
                stackSize
            );

            e.setCancelled(willCancel);
        }
    }
}
