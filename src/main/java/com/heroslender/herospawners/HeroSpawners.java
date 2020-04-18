package com.heroslender.herospawners;

import com.heroslender.herospawners.commands.HeroSpawnersCommand;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.StorageController;
import com.heroslender.herospawners.listeners.HologramListener;
import com.heroslender.herospawners.listeners.SilkSpawnersListener;
import com.heroslender.herospawners.listeners.SpawnerListener;
import com.heroslender.herospawners.listeners.SpawnerSpawnListener;
import com.heroslender.herospawners.mobstacker.*;
import com.heroslender.herospawners.services.*;
import com.heroslender.herospawners.utils.Metrics;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class HeroSpawners extends JavaPlugin {
    @Getter private static HeroSpawners instance;

    @Getter private final Executor executor = ForkJoinPool.commonPool();
    @Getter private final StorageController storageController;
    @Getter private final ConfigurationController configurationController;
    @Getter private MobStackerSupport mobStackerSupport;
    @Getter private boolean shutingDown = true;

    public HeroSpawners() {
        super();
        instance = this;
        saveDefaultConfig();

        StorageService storageService;
        if (getConfig().getBoolean("MySql.usar", false))
            storageService = new StorageServiceMySqlImpl();
        else
            storageService = new StorageServiceSQLiteImpl();
        storageController = new StorageController(storageService, getExecutor());

        ConfigurationService configurationService = new ConfigurationServiceImpl();
        configurationController = new ConfigurationController(configurationService);
    }

    public void onEnable() {
        configurationController.init();
        storageController.init();

        getServer().getPluginManager().registerEvents(new HologramListener(configurationController), this);

        // StackMobs
        if (Bukkit.getServer().getPluginManager().getPlugin("MobStacker2") != null)
            mobStackerSupport = new MobStacker2();
        else if (Bukkit.getServer().getPluginManager().getPlugin("StackMob") != null)
            mobStackerSupport = new StackMob();
        else if (Bukkit.getServer().getPluginManager().getPlugin("TintaStack") != null)
            mobStackerSupport = new TintaStack();
        else if (Bukkit.getServer().getPluginManager().getPlugin("JH_StackMobs") != null)
            mobStackerSupport = new JhMobStacker();
        else if (Bukkit.getServer().getPluginManager().getPlugin("ObyStack") != null)
            mobStackerSupport = new ObyStack();
        else
            mobStackerSupport = new SemMobStacker();

        // listeners
        getServer().getPluginManager().registerEvents(new SpawnerSpawnListener(), this);
        if (getServer().getPluginManager().getPlugin("SilkSpawners") != null)
            getServer().getPluginManager().registerEvents(new SilkSpawnersListener(configurationController, storageController), this);
        else {
            getServer().getPluginManager().registerEvents(new SpawnerListener(configurationController, storageController), this);
        }

        // Metrics - https://bstats.org/plugin/bukkit/HeroSpawners
        new Metrics(this).submitData();

        // Colocar o server como ligado(Prevenir dups em reinicios)
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> shutingDown = false);

        ((Logger) LogManager.getRootLogger()).addFilter(new AbstractFilter() {
            @Override
            public Result filter(LogEvent event) {
                if (event != null
                        && event.getMessage() != null
                        && event.getMessage().getFormattedMessage() != null
                        && event.getMessage().getFormattedMessage().contains("Skipping BlockEntity with id")) {
                    return Result.DENY;
                }
                return Result.NEUTRAL;
            }
        });

        getCommand("herospawners").setExecutor(new HeroSpawnersCommand());

        getLogger().info("Plugin carregado!");
    }

    public void onDisable() {
        shutingDown = true;
        storageController.stop();
        configurationController.stop();
    }
}


