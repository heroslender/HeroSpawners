package com.heroslender.herospawners;

import com.heroslender.herospawners.commands.HeroSpawnersCommand;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.SpawnerController;
import com.heroslender.herospawners.listeners.*;
import com.heroslender.herospawners.mobstacker.*;
import com.heroslender.herospawners.mobstacker.strategies.*;
import com.heroslender.herospawners.services.StorageService;
import com.heroslender.herospawners.services.StorageServiceMySqlImpl;
import com.heroslender.herospawners.services.StorageServiceSQLiteImpl;
import com.heroslender.herospawners.spawners.commands.SpawnerCommand;
import com.heroslender.herospawners.spawners.listeners.SpawnerBlockListener;
import com.heroslender.herospawners.utils.Metrics;
import lombok.Getter;
import lombok.val;
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

        if (getConfig().getBoolean("holograma.ativar", true)) {
            if (getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
                final HologramListener hologramListener =
                        new HologramListener(getConfigurationController(), getSpawnerController());
                getServer().getPluginManager().registerEvents(hologramListener, this);
                getLogger().log(Level.INFO, "HolographicDisplays encontrado! Ativando hologramas nos spawners.");
            } else {
                getLogger().log(Level.WARNING, "HolographicDisplays não foi encontrado! Desativado hologramas nos spawners.");
            }
        } else {
            getLogger().log(Level.INFO, "Hologramas nos spawners desativado!");
        }

        if (getConfig().getBoolean("interact.ativar", true)) {
            getServer().getPluginManager().registerEvents(new InteractListener(getConfigurationController()), this);
            getLogger().log(Level.INFO, "Mostrar infos ao clicar no spawner ativado!");
        } else {
            getLogger().log(Level.INFO, "Mostrar infos ao clicar no spawner desativado!");
        }

        // StackMobs
        this.mobStacker = computeMobStackerStrategy();

        // listeners
        getServer().getPluginManager().registerEvents(new SpawnerSpawnListener(), this);
        if (getServer().getPluginManager().getPlugin("SilkSpawners") != null) {
            getServer().getPluginManager().registerEvents(
                    new SilkSpawnersListener(configurationController, spawnerController),
                    this
            );
        } else if (getConfigurationController().isSpawnersEnabled()) {
            getServer().getPluginManager().registerEvents(
                    new SpawnerBlockListener(configurationController, spawnerController),
                    this
            );
            val spawnerCommand = new SpawnerCommand();
            getCommand("spawners").setExecutor(spawnerCommand);
            getCommand("spawners").setTabCompleter(spawnerCommand);
        } else {
            getServer().getPluginManager().registerEvents(new SpawnerListener(configurationController, spawnerController), this);
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

    private MobStackerStrategy computeMobStackerStrategy() {
        final MobStackerStrategy strategy;

        if (Bukkit.getServer().getPluginManager().getPlugin("MobStacker2") != null)
            strategy = new MobStacker2();
        else if (Bukkit.getServer().getPluginManager().getPlugin("StackMob") != null)
            strategy = new StackMob();
        else if (Bukkit.getServer().getPluginManager().getPlugin("TintaStack") != null)
            strategy = new TintaStack();
        else if (Bukkit.getServer().getPluginManager().getPlugin("JH_StackMobs") != null){
            MobStackerStrategy jh;
            try {
                Class.forName("ultils.StackAll");
                jh = new JhStackMobs();
            } catch (ClassNotFoundException e) {
                jh = new JhStackMobs2();
            }

            strategy = jh;
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


