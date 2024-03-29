package com.heroslender.herospawners.feature.mobstacker.strategies;

import com.heroslender.herospawners.feature.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.models.ISpawner;
import me.tintastack.api.MobsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

/**
 * Created by Heroslender.
 */
public class TintaStack implements MobStackerStrategy {

    public TintaStack() {
        Bukkit.getLogger().info("[herospawners] TintaStack foi encontrado!");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        MobsAPI.criarStackEntidade(entity, quantidade);
        return false;
    }
}
