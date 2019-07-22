package com.heroslender.herospawners.mobstacker;

import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.entity.Entity;

/**
 * Created by Heroslender.
 */
public interface MobStackerSupport {

     boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade);
}
