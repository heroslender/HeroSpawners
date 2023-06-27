package com.heroslender.herospawners.feature.spawner.silkspawners;

import com.heroslender.herospawners.feature.spawner.SpawnerHandlerStrategy;
import com.heroslender.herospawners.internal.HeroPlugin;
import com.heroslender.herospawners.service.ConfigurationService;
import com.heroslender.herospawners.service.SpawnerService;
import org.jetbrains.annotations.NotNull;

public class SilkSpawnersSpawnerHandlerStrategy extends SpawnerHandlerStrategy {
    public SilkSpawnersSpawnerHandlerStrategy(@NotNull HeroPlugin plugin) {
        super(plugin);

        addListener(new SilkSpawnersListener(
            plugin.getService(ConfigurationService.class),
            plugin.getService(SpawnerService.class)
        ));
    }
}
