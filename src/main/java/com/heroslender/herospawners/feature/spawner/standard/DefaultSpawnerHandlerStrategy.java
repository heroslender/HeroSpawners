package com.heroslender.herospawners.feature.spawner.standard;

import com.heroslender.herospawners.feature.spawner.SpawnerHandlerStrategy;
import com.heroslender.herospawners.internal.HeroPlugin;
import com.heroslender.herospawners.service.ConfigurationService;
import com.heroslender.herospawners.service.SpawnerService;
import org.jetbrains.annotations.NotNull;

public class DefaultSpawnerHandlerStrategy extends SpawnerHandlerStrategy {
    public DefaultSpawnerHandlerStrategy(@NotNull HeroPlugin plugin) {
        super(plugin);

        addListener(new SpawnerListener(
            plugin.getService(ConfigurationService.class),
            plugin.getService(SpawnerService.class)
        ));
    }
}
