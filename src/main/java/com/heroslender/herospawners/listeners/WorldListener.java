package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.controllers.SpawnerController;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

@RequiredArgsConstructor
public class WorldListener implements Listener {
    private final SpawnerController spawnerController;

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        spawnerController.load(e.getWorld());
    }
}
