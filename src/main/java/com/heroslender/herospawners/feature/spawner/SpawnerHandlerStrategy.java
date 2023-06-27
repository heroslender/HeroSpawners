package com.heroslender.herospawners.feature.spawner;

import com.heroslender.herospawners.feature.Feature;
import com.heroslender.herospawners.internal.HeroPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class SpawnerHandlerStrategy extends Feature {
    protected SpawnerHandlerStrategy(@NotNull HeroPlugin plugin) {
        super(plugin);
    }
}
