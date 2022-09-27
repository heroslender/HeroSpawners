package com.heroslender.herospawners;

import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.SpawnerController;
import com.heroslender.herospawners.hologram.HologramFactory;
import com.heroslender.herospawners.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.mobstacker.strategies.*;
import com.heroslender.herospawners.services.StorageService;
import com.heroslender.herospawners.services.StorageServiceMySqlImpl;
import com.heroslender.herospawners.services.StorageServiceSQLiteImpl;
import com.heroslender.herospawners.utils.Metrics;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;

public class HeroSpawners extends JavaPlugin {
    public static final Material SPAWNER_TYPE;

    @Getter private static HeroSpawners instance;

    @Getter private final Executor executor = ForkJoinPool.commonPool();
    @Getter private final SpawnerController spawnerController;
    @Getter private final ConfigurationController configurationController;
    @Getter private MobStackerStrategy mobStacker;
    @Getter HologramFactory hologramFactory;
    @Getter private boolean shutingDown = true;

    static {
        Material mat;
        try {
            mat = Material.valueOf("SPAWNER");
            System.out.println(mat);
        } catch (IllegalArgumentException e) {
            mat = Material.MOB_SPAWNER;
        }

        SPAWNER_TYPE = mat;
    }

    public HeroSpawners() {
        super();
        instance = this;
        saveDefaultConfig();

        StorageService storageService;
        if (getConfig().getBoolean("MySql.usar", false))
            storageService = new StorageServiceMySqlImpl();
        else
            storageService = new StorageServiceSQLiteImpl();
        spawnerController = new SpawnerController(storageService, getExecutor());

        configurationController = new ConfigurationController();
    }

    @Override
    public void onEnable() {
        configurationController.init();
        spawnerController.init();

        this.mobStacker = computeMobStackerStrategy();

        final Bootstrap bootstrap = new Bootstrap(getConfig(), this);
        bootstrap.setupSpawnerSpawnListener();
        bootstrap.setupSpawnerBlockListener();

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

        bootstrap.setupCommands();
        bootstrap.setupHolograms();
        bootstrap.setupSpawnerInfoOnInteract();

        try {
            // Metrics - https://bstats.org/plugin/bukkit/HeroSpawners
            new Metrics(this).submitData();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Falha ao enviar dados para o servidor de estatisticas", e);
        }

        // Colocar o server como ligado(Prevenir dups em reinicios)
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> shutingDown = false);

        getLogger().info("Plugin carregado!");
    }

    private MobStackerStrategy computeMobStackerStrategy() {
        final MobStackerStrategy strategy;

        if (Bukkit.getPluginManager().getPlugin("MobStacker2") != null)
            strategy = new MobStacker2();
        else if (Bukkit.getPluginManager().getPlugin("StackMob") != null) {
            MobStackerStrategy strat;
            try {
                Class.forName("uk.antiperson.stackmob.api.EntityManager");
                strat = new StackMob2();
            } catch (ClassNotFoundException e) {
                strat = new StackMob();
            }
            strategy = strat;
        } else if (Bukkit.getPluginManager().getPlugin("TintaStack") != null)
            strategy = new TintaStack();
        else if (Bukkit.getPluginManager().getPlugin("JH_StackMobs") != null) {
            strategy = new JhStackMobs();
        } else
            strategy = new NoMobStacker();

        return strategy;
    }

    public boolean shutdownCheck(final Cancellable event, final Player player) {
        if (isShutingDown()) {
            event.setCancelled(true);
            player.sendMessage("§cNão é possivel colocar spawners quando o servidor esta a ligar/desligar.");
            return true;
        }

        return false;
    }

    @Override
    public void onDisable() {
        shutingDown = true;

        HandlerList.unregisterAll(this);

        spawnerController.stop();
        configurationController.stop();
    }
}


