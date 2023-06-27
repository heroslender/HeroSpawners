package com.heroslender.herospawners;

import com.heroslender.herospawners.commands.HeroSpawnersCommand;
import com.heroslender.herospawners.feature.chatinfo.ChatInfoFeature;
import com.heroslender.herospawners.feature.hologram.HologramFeature;
import com.heroslender.herospawners.feature.mobstacker.MobstackerFeature;
import com.heroslender.herospawners.feature.spawner.SpawnerFeature;
import com.heroslender.herospawners.internal.HeroPlugin;
import com.heroslender.herospawners.service.ConfigurationService;
import com.heroslender.herospawners.service.SpawnerService;
import com.heroslender.herospawners.service.storage.StorageService;
import com.heroslender.herospawners.service.storage.StorageServiceMySqlImpl;
import com.heroslender.herospawners.service.storage.StorageServiceSQLiteImpl;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.logging.Level;

public class HeroSpawners extends HeroPlugin {
    public static final Material SPAWNER_TYPE;

    @Getter private static HeroSpawners instance;

    static {
        Material mat;
        try {
            mat = Material.valueOf("SPAWNER");
        } catch (IllegalArgumentException e) {
            mat = Material.MOB_SPAWNER;
        }

        SPAWNER_TYPE = mat;
    }

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


        provideService(ConfigurationService.class, new ConfigurationService(this));
        provideService(StorageService.class, storageService);
        provideService(SpawnerService.class, new SpawnerService(storageService));

        provideService(HologramFeature.class, new HologramFeature(this));
        provideService(ChatInfoFeature.class, new ChatInfoFeature(this));
        provideService(SpawnerFeature.class, new SpawnerFeature(this));
        provideService(MobstackerFeature.class, new MobstackerFeature(this));
    }

    @Override
    public void enable() {
        getCommand("herospawners").setExecutor(new HeroSpawnersCommand());

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

        try { // Metrics - https://bstats.org/plugin/bukkit/HeroSpawners
            new Metrics(this).submitData();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Falha ao enviar dados para o servidor de estatisticas", e);
        }

        registerListener(new Listener() {
            @EventHandler
            public void onWorldLoad(WorldLoadEvent e) {
                getSpawnerService().load(e.getWorld());
            }
        });

        getLogger().info("Plugin carregado!");

        // Colocar o server como ligado(Prevenir dups em reinicios)
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> shutingDown = false);
    }

    public boolean shutdownCheck(final Cancellable event, final Player player) {
        if (isShutingDown()) {
            event.setCancelled(true);
            player.sendMessage("§cNão é possivel utilizar spawners quando o servidor esta a ligar/desligar.");
            return true;
        }

        return false;
    }

    @Override
    public void disable() {
        shutingDown = true;
    }

    public ConfigurationService getConfigurationController() {
        return getService(ConfigurationService.class);
    }

    public SpawnerService getSpawnerService() {
        return getService(SpawnerService.class);
    }
}


