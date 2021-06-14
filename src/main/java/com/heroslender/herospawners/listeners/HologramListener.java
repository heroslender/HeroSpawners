package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.SpawnerController;
import com.heroslender.herospawners.utils.HologramFacade;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HologramListener extends HologramFacade implements Listener {

    public HologramListener(ConfigurationController config, SpawnerController storage) {
        super(config, storage);
    }

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }

        setHologram(e.getPlayer());
    }
}
