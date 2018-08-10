package com.heroslender.HeroSpawners.MobStackerSuport;

import com.heroslender.HeroSpawners.Spawner.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * Created by Heroslender.
 */
public class SemMobStacker implements MobStackerSuport {

    public SemMobStacker() {
        Bukkit.getLogger().info("[HeroSpawners] Nao foi encontrado um plugin de MobStacker, a usar o metodo padrao.");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        for (int i = 0; i<quantidade; i++)
            entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        return true;
    }
}
