package com.heroslender.herospawners.feature.mobstacker.strategies;

import com.heroslender.herospawners.feature.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import uk.antiperson.stackmob.api.EntityManager;

/**
 * Created by Heroslender.
 */
public class StackMob2 implements MobStackerStrategy {
    EntityManager em;

    public StackMob2() {
        Bukkit.getLogger().info("[herospawners] StackMob foi encontrado!");
        em = new EntityManager((uk.antiperson.stackmob.StackMob) Bukkit.getPluginManager().getPlugin("StackMob"));
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        em.addNewStack(entity, quantidade);
        return false;
    }
}
