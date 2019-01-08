package com.heroslender.herospawners;

import com.heroslender.herospawners.events.*;
import com.heroslender.herospawners.mobstacker.*;
import com.heroslender.herospawners.storage.Storage;
import com.heroslender.herospawners.storage.StorageMySql;
import com.heroslender.herospawners.storage.StorageSqlLite;
import com.heroslender.herospawners.utils.Config;
import com.heroslender.herospawners.utils.Metrics;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class HeroSpawners extends JavaPlugin {
    @Getter private static HeroSpawners instance;

    @Getter public Set<Location> newSpawner = new HashSet<>();
    @Getter private MobStackerSuport mobStackerSuport;
    @Getter private boolean shutingDown = true;

    @Getter private Storage storage;

    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Inicializar a config
        Config.init();

        getLogger().info("Hologramas ativados apenas ao passar o mouse!");
        getServer().getPluginManager().registerEvents(new HologramListener(), this);

        // Base de dados
        if (getConfig().getBoolean("MySql.usar", false))
            storage = new StorageMySql();
        else
            storage = new StorageSqlLite();

        // StackMobs
        if (Bukkit.getServer().getPluginManager().getPlugin("MobStacker2") != null)
            mobStackerSuport = new MobStacker2();
        else if (Bukkit.getServer().getPluginManager().getPlugin("StackMob") != null)
            mobStackerSuport = new StackMob();
        else if (Bukkit.getServer().getPluginManager().getPlugin("TintaStack") != null)
            mobStackerSuport = new TintaStack();
        else if (Bukkit.getServer().getPluginManager().getPlugin("JH_StackMobs") != null)
            mobStackerSuport = new JhMobStacker();
        else if (Bukkit.getServer().getPluginManager().getPlugin("ObyStack") != null)
            mobStackerSuport = new ObyStack();
        else
            mobStackerSuport = new SemMobStacker();

        // Eventos
        getServer().getPluginManager().registerEvents(new SpawnerEvent(), this);
        if (getServer().getPluginManager().getPlugin("SilkSpawners") != null)
            getServer().getPluginManager().registerEvents(new SilkSpawnersListener(), this);
        else {
            getServer().getPluginManager().registerEvents(new BreakEvent(), this);
            getServer().getPluginManager().registerEvents(new PlaceEvent(), this);
        }

        // Metrics - https://bstats.org/plugin/bukkit/HeroSpawners
        new Metrics(this).submitData();

        // Colocar o server como ligado(Prevenir dups em reinicios)
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> shutingDown = false);

        ((Logger) LogManager.getRootLogger()).addFilter(new AbstractFilter() {
            @Override
            public Result filter(LogEvent event) {
                if (event != null && event.getMessage() != null && event.getMessage().getFormattedMessage().contains("Skipping BlockEntity with id")) {
                    return Result.DENY;
                }
                return Result.NEUTRAL;
            }
        });

        getLogger().info("Plugin carregado!");
    }

    public void onDisable() {
        shutingDown = true;
        if (storage != null) storage.onDisable();
    }
}


