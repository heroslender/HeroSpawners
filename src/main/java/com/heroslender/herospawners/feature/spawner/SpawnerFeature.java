package com.heroslender.herospawners.feature.spawner;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.feature.Feature;
import com.heroslender.herospawners.feature.spawner.herospawners.HeroSpawnersSpawnerHandlerStrategy;
import com.heroslender.herospawners.feature.spawner.silkspawners.SilkSpawnersSpawnerHandlerStrategy;
import com.heroslender.herospawners.feature.spawner.standard.DefaultSpawnerHandlerStrategy;
import com.heroslender.herospawners.service.ConfigurationService;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class SpawnerFeature extends Feature {
    private SpawnerHandlerStrategy handlerStrategy;

    public SpawnerFeature(@NotNull HeroSpawners plugin) {
        super(plugin);
    }

    @Override
    public void enableFeature() {
        if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners")) {
            if (!(handlerStrategy instanceof SilkSpawnersSpawnerHandlerStrategy)) {
                this.handlerStrategy = new SilkSpawnersSpawnerHandlerStrategy(getPlugin());
            }
        } else if (getPlugin().getService(ConfigurationService.class).isSpawnersEnabled()) {
            if (!(handlerStrategy instanceof HeroSpawnersSpawnerHandlerStrategy)) {
                this.handlerStrategy = new HeroSpawnersSpawnerHandlerStrategy(getPlugin());
            }
        } else {
            if (!(handlerStrategy instanceof DefaultSpawnerHandlerStrategy)) {
                this.handlerStrategy = new DefaultSpawnerHandlerStrategy(getPlugin());
            }
        }

        handlerStrategy.enable();
    }

    @Override
    public void disableFeature() {
        handlerStrategy.disable();
    }
}
