package com.heroslender.herospawners;

import com.heroslender.herospawners.commands.HeroSpawnersCommand;
import com.heroslender.herospawners.listeners.*;
import com.heroslender.herospawners.spawners.commands.SpawnerCommand;
import com.heroslender.herospawners.spawners.listeners.SpawnerBlockListener;
import com.heroslender.herospawners.tasks.HologramUpdateTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class Bootstrap {
    @Getter private final FileConfiguration config;
    @Getter private final HeroSpawners heroSpawners;

    public void setupHolograms() {
        if (!getConfig().getBoolean("holograma.ativar", true)) {
            getLogger().log(Level.INFO, "Hologramas nos spawners desativado!");
            return;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().log(
                Level.WARNING,
                "HolographicDisplays não foi encontrado! Desativado hologramas nos spawners."
            );
            return;
        }

        getLogger().log(Level.INFO, "HolographicDisplays encontrado! Ativando hologramas nos spawners.");

        switch (getConfig().getString("holograma.metodo", "TIMER").toUpperCase(Locale.ROOT)) {
            case "TIMER":
                if (!getConfig().isSet("holograma.delay")) {
                    getConfig().set("holograma.delay", 20);
                    heroSpawners.saveConfig();
                }

                final int delay = getConfig().getInt("holograma.delay", 20);
                final Runnable task = new HologramUpdateTask(
                    heroSpawners.getConfigurationController(),
                    heroSpawners.getSpawnerController()
                );

                Bukkit.getScheduler().runTaskTimer(heroSpawners, task, delay, delay);
                break;
            case "MOVE_EVENT":
                final HologramListener hologramListener = new HologramListener(
                    heroSpawners.getConfigurationController(),
                    heroSpawners.getSpawnerController()
                );
                Bukkit.getPluginManager().registerEvents(hologramListener, heroSpawners);
                break;
        }
    }

    public void setupSpawnerInfoOnInteract() {
        if (!getConfig().getBoolean("interact.ativar", true)) {
            getLogger().log(Level.INFO, "Mostrar infos ao clicar no spawner desativado!");
            return;
        }

        final InteractListener interactListener = new InteractListener(heroSpawners.getConfigurationController());
        Bukkit.getPluginManager().registerEvents(interactListener, heroSpawners);
        getLogger().log(Level.INFO, "Mostrar infos ao clicar no spawner ativado!");
    }

    public void setupSpawnerSpawnListener() {
        Bukkit.getPluginManager().registerEvents(new SpawnerSpawnListener(), heroSpawners);
    }

    public void setupSpawnerBlockListener() {
        final Listener spawnerBlockListener;
        if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners")) {
            spawnerBlockListener = new SilkSpawnersListener(
                heroSpawners.getConfigurationController(),
                heroSpawners.getSpawnerController()
            );
        } else if (heroSpawners.getConfigurationController().isSpawnersEnabled()) {
            spawnerBlockListener = new SpawnerBlockListener(
                heroSpawners.getConfigurationController(),
                heroSpawners.getSpawnerController()
            );
        } else {
            spawnerBlockListener = new SpawnerListener(
                heroSpawners.getConfigurationController(),
                heroSpawners.getSpawnerController()
            );
        }
        Bukkit.getPluginManager().registerEvents(spawnerBlockListener, heroSpawners);

        final Listener worldListener = new WorldListener(heroSpawners.getSpawnerController());
        Bukkit.getPluginManager().registerEvents(worldListener, heroSpawners);
    }

    public void setupCommands() {
        if (!Bukkit.getPluginManager().isPluginEnabled("SilkSpawners")
            && heroSpawners.getConfigurationController().isSpawnersEnabled()) {

            val spawnerCommand = new SpawnerCommand();
            heroSpawners.getCommand("spawners").setExecutor(spawnerCommand);
            heroSpawners.getCommand("spawners").setTabCompleter(spawnerCommand);
        }

        heroSpawners.getCommand("herospawners").setExecutor(new HeroSpawnersCommand());
    }

    private Logger getLogger() {
        return heroSpawners.getLogger();
    }
}
