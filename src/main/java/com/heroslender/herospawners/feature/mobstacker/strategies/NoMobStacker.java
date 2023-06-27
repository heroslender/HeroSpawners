package com.heroslender.herospawners.feature.mobstacker.strategies;

import com.heroslender.herospawners.feature.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

/**
 * Created by Heroslender.
 */
public class NoMobStacker implements MobStackerStrategy {

    public NoMobStacker() {
        Bukkit.getLogger().info("[herospawners] Nao foi encontrado um plugin de MobStacker, a usar o metodo padrao.");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        for (int i = 0; i<quantidade; i++)
            entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        return true;
    }
}
