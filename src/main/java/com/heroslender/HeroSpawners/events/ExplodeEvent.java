package com.heroslender.HeroSpawners.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

/**
 * Created by Heroslender.
 */
public class ExplodeEvent implements Listener {

    @EventHandler
    private void onSpawnerExplode(BlockExplodeEvent e) {
        if (e.getBlock().getType() == Material.MOB_SPAWNER)
            e.setCancelled(true);
    }
}
