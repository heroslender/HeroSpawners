package com.heroslender.herospawners.api.events;

import com.heroslender.herospawners.models.ISpawner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;

/**
 * Called when a stacked spawner spawns an entity stack.
 * <p>
 * If not cancelled, will use the standard mobstacker support.
 */
@RequiredArgsConstructor
public class SpawnerSpawnStackEvent extends EventWrapper implements Cancellable {
    /**
     * The spawner spawning the entity
     */
    @Getter private final ISpawner spawner;

    /**
     * The type of the spawned entity
     */
    @Getter private final EntityType entityType;

    /**
     * The size of the entity stack
     */
    @Getter private final int stackSize;

    private boolean canceled = false;

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(boolean cancel) {
        canceled = cancel;
    }
}
