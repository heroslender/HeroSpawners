package com.heroslender.herospawners.feature.hologram.strategy;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.feature.hologram.HologramFacade;
import com.heroslender.herospawners.feature.hologram.HologramFactory;
import com.heroslender.herospawners.internal.HeroPlugin;
import com.heroslender.herospawners.service.ConfigurationService;
import com.heroslender.herospawners.service.SpawnerService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveHologramDisplayStategy extends HologramFacade implements Listener, HologramDisplayStrategy {
    private final HeroPlugin plugin;

    public PlayerMoveHologramDisplayStategy(HeroPlugin plugin, HologramFactory hologramFactory) {
        super(plugin.getService(ConfigurationService.class), plugin.getService(SpawnerService.class), hologramFactory);
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }

        setHologram(e.getPlayer());
    }
}
