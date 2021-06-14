package com.heroslender.herospawners.tasks;

import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.SpawnerController;
import com.heroslender.herospawners.utils.HologramFacade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HologramUpdateTask extends HologramFacade implements Runnable {

    public HologramUpdateTask(ConfigurationController config, SpawnerController storage) {
        super(config, storage);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setHologram(player);
        }
    }
}
