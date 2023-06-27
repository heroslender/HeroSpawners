package com.heroslender.herospawners.feature.spawner.herospawners;

import com.heroslender.herospawners.feature.FeatureInitializationException;
import com.heroslender.herospawners.feature.spawner.SpawnerHandlerStrategy;
import com.heroslender.herospawners.internal.HeroPlugin;
import com.heroslender.herospawners.service.ConfigurationService;
import com.heroslender.herospawners.service.SpawnerService;
import com.heroslender.herospawners.feature.spawner.herospawners.commands.SpawnerCommand;
import com.heroslender.herospawners.feature.spawner.herospawners.listeners.SpawnerBlockListener;
import lombok.val;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

public class HeroSpawnersSpawnerHandlerStrategy extends SpawnerHandlerStrategy {
    private static final String COMMAND_NAME = "spawners";

    public HeroSpawnersSpawnerHandlerStrategy(@NotNull HeroPlugin plugin) {
        super(plugin);

        addListener(new SpawnerBlockListener(
            plugin.getService(ConfigurationService.class),
            plugin.getService(SpawnerService.class)
        ));
    }

    @Override
    public void enableFeature() {
        val spawnerCommand = new SpawnerCommand();
        PluginCommand pluginCommand = getPlugin().getCommand(COMMAND_NAME);
        pluginCommand.setExecutor(spawnerCommand);
        pluginCommand.setTabCompleter(spawnerCommand);
    }

    @Override
    public void disableFeature() {
        super.disableFeature();

        PluginCommand pluginCommand = getPlugin().getCommand(COMMAND_NAME);
        pluginCommand.setExecutor(null);
        pluginCommand.setTabCompleter(null);
    }
}
