package com.heroslender.herospawners.mobstacker.strategies;

import com.heroslender.herospawners.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import uk.antiperson.stackmob.entity.EntityManager;
import uk.antiperson.stackmob.entity.StackEntity;

/**
 * Created by Heroslender.
 */
public class StackMob implements MobStackerStrategy {
    EntityManager em;

    public StackMob() {
        Bukkit.getLogger().info("[herospawners] StackMob foi encontrado!");
        em = ((uk.antiperson.stackmob.StackMob) Bukkit.getPluginManager().getPlugin("StackMob")).getEntityManager();
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        final StackEntity stackEntity = em.registerStackedEntity((LivingEntity) entity);
        stackEntity.setSize(quantidade);
        return false;
    }
}
