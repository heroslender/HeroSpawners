package com.heroslender.HeroSpawners.MobStackerSuport;

import com.heroslender.HeroSpawners.Spawner.ISpawner;
import org.bukkit.entity.Entity;

/**
 * Created by Heroslender.
 */
public interface MobStackerSuport {

     boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade);
}
