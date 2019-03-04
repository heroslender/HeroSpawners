package com.heroslender.herospawners.mobstacker;

import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

/**
 * Created by Heroslender.
 */
public class SemMobStacker implements MobStackerSuport {

    public SemMobStacker() {
        Bukkit.getLogger().info("[herospawners] Nao foi encontrado um plugin de MobStacker, a usar o metodo padrao.");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        for (int i = 0; i<quantidade; i++)
            entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        return true;
    }
}
