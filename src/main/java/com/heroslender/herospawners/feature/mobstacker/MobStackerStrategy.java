package com.heroslender.herospawners.feature.mobstacker;

import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.entity.Entity;

/**
 * Created by Heroslender.
 */
public interface MobStackerStrategy {

     boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade);
}
