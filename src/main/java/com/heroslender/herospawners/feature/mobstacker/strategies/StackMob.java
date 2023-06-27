package com.heroslender.herospawners.feature.mobstacker.strategies;

import com.heroslender.herospawners.feature.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import uk.antiperson.stackmob.entity.EntityManager;
import uk.antiperson.stackmob.entity.StackEntity;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Heroslender.
 */
public class StackMob implements MobStackerStrategy {
    EntityManager em;

    public StackMob() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Bukkit.getLogger().info("[herospawners] StackMob foi encontrado!");
        Plugin stackMob = Bukkit.getPluginManager().getPlugin("StackMob");
        em = (EntityManager) stackMob.getClass().getMethod("getEntityManager").invoke(stackMob);
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        final StackEntity stackEntity = em.registerStackedEntity((LivingEntity) entity);
        stackEntity.setSize(quantidade);
        return false;
    }
}
