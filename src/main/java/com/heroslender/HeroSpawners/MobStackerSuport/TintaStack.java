package com.heroslender.HeroSpawners.MobStackerSuport;

import com.heroslender.HeroSpawners.Spawner.ISpawner;
import me.tintastack.api.MobsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

/**
 * Created by Heroslender.
 */
public class TintaStack implements MobStackerSuport {

    public TintaStack() {
        Bukkit.getLogger().info("[HeroSpawners] TintaStack foi encontrado!");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        MobsAPI.criarStackEntidade(entity, quantidade);
        return false;
    }
}
