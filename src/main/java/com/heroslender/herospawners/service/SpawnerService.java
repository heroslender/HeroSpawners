package com.heroslender.herospawners.service;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.service.Service;
import com.heroslender.herospawners.service.storage.StorageService;
import com.heroslender.herospawners.utils.Utilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SpawnerService implements Service {
    // Delay entre updates na base de dados, 5 minutos
    private static final long UPDATE_DELAY = 20 * 60 * 5;
    private final StorageService storageService;
    private final Executor executor = ForkJoinPool.commonPool();

    private final Map<Location, ISpawner> cachedSpawners = new ConcurrentHashMap<>();

    @Getter(AccessLevel.PRIVATE) private final Logger logger = HeroSpawners.getInstance().getLogger();

    @Override
    public void enable() {
        cachedSpawners.clear();

        Map<Location, ISpawner> spawners = storageService.getSpawners();
        HeroSpawners.getInstance().getLogger().log(Level.INFO, "[Storage] Foram carregados {0} spawners da base de dados.", spawners.size());

        Bukkit.getScheduler().runTaskTimerAsynchronously(HeroSpawners.getInstance(), this::save, UPDATE_DELAY, UPDATE_DELAY);

        cachedSpawners.putAll(spawners);
    }

    public void load(World world) {
        HeroSpawners.getInstance().getLogger().log(
            Level.INFO,
            "[Storage] Carregando spawners no mundo {0}...",
            world.getName()
        );

        Map<Location, ISpawner> spawners = storageService.getSpawners(world.getName());
        cachedSpawners.putAll(spawners);

        HeroSpawners.getInstance().getLogger().log(
            Level.INFO,
            "[Storage] Foram carregados {0} spawners no mundo {1} da base de dados.",
            new Object[]{spawners.size(), world.getName()}
        );
    }

    public void save() {
        for (ISpawner spawner : cachedSpawners.values()) {
            if (((Spawner) spawner).isUpdateRequired()) {
                updateSpawner(spawner);
            }
        }
    }

    public ISpawner saveSpawner(@NotNull final Player owner, @NotNull final CreatureSpawner spawner, final int amount) {
        Objects.requireNonNull(owner, "owner is null");
        Objects.requireNonNull(spawner, "spawner is null");

        final ISpawner s = new Spawner(
            owner.getName(),
            spawner.getBlock().getLocation(),
            amount
        );

        saveSpawner(s);

        // Reset the spawn delay, preventing grinding by placing new spawners.
        // This is because when placing a new spawner, the default spawn delay is
        // set to 20 ticks(1 second), and after that first spawn, it resets to the regular.
        spawner.setDelay(200 + Utilities.getRandom().nextInt(600));

        getLogger().log(
            Level.FINEST,
            "{0} created stack on {1}",
            new Object[]{owner, s}
        );

        return s;
    }

    public void saveSpawner(ISpawner spawner) {
        cachedSpawners.put(spawner.getLocation(), spawner);
        executor.execute(() -> storageService.save(spawner));
    }

    public void updateSpawner(@NotNull final Player who, @NotNull final ISpawner spawner, final int newAmount) {
        final int prevAmount = spawner.getAmount();

        spawner.setAmount(newAmount);

        if (prevAmount < newAmount) {
            try {
                Location loc = spawner.getLocation();
                loc.getWorld().spigot().playEffect(
                    loc,
                    Effect.WITCH_MAGIC,
                    1,
                    0,
                    1.0F, 1.0F, 1.0F,
                    1.0F,
                    200,
                    10
                );
            } catch (NoSuchFieldError error) {
                // ignored, since 1.13
            }

            getLogger().log(
                Level.FINEST,
                "{0} stacked +{1} on {2}",
                new Object[]{who, newAmount - prevAmount, spawner}
            );
        } else if (newAmount <= 0) {
            deleteSpawner(spawner);

            getLogger().log(
                Level.FINEST,
                "{0} broken all {1} from {2}",
                new Object[]{who, prevAmount, spawner}
            );
        } else {
            getLogger().log(
                Level.FINEST,
                "{0} broken {1} on {2}",
                new Object[]{who, newAmount - prevAmount, spawner}
            );
        }
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

    public Map<Location, ISpawner> getSpawners() {
        return cachedSpawners;
    }

    @Override
    public void disable() {
        save();
        cachedSpawners.clear();
    }
}
