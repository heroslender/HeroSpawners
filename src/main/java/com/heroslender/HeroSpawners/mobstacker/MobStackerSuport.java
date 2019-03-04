package com.heroslender.herospawners.mobstacker;

import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.entity.Entity;

/**
 * Created by Heroslender.
 */
public interface MobStackerSuport {

     boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade);
}
