package com.heroslender.HeroSpawners.MobStackerSuport;

import com.heroslender.HeroSpawners.Spawner.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import uk.antiperson.stackmob.api.EntityManager;

/**
 * Created by Heroslender.
 */
public class StackMob implements MobStackerSuport {
    EntityManager em;

    public StackMob() {
        Bukkit.getLogger().info("[HeroSpawners] StackMob foi encontrado!");
        em = new EntityManager((uk.antiperson.stackmob.StackMob) Bukkit.getPluginManager().getPlugin("StackMob"));
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        em.addNewStack(entity, quantidade);
        return false;
    }
}
