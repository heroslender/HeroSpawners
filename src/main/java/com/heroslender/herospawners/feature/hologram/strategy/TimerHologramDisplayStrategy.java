package com.heroslender.herospawners.feature.hologram.strategy;

import com.heroslender.herospawners.feature.hologram.HologramFacade;
import com.heroslender.herospawners.feature.hologram.HologramFactory;
import com.heroslender.herospawners.internal.HeroPlugin;
import com.heroslender.herospawners.service.ConfigurationService;
import com.heroslender.herospawners.service.SpawnerService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TimerHologramDisplayStrategy extends HologramFacade implements HologramDisplayStrategy, Runnable {
    private final HeroPlugin plugin;
    private BukkitTask task;

    public TimerHologramDisplayStrategy(HeroPlugin plugin, HologramFactory hologramFactory) {
        super(plugin.getService(ConfigurationService.class), plugin.getService(SpawnerService.class), hologramFactory);
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().isSet("holograma.delay")) {
            plugin.getConfig().set("holograma.delay", 20);
            plugin.saveConfig();
        }

        final int delay = plugin.getConfig().getInt("holograma.delay", 20);
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, delay, delay);
    }

    @Override
    public void disable() {
        task.cancel();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setHologram(player);
        }
    }
}
