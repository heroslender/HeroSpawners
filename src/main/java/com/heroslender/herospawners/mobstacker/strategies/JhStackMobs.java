package com.heroslender.herospawners.mobstacker.strategies;

import JH_StackMobs.API;
import JH_StackMobs.Main;
import JH_StackMobs.utils.IaRemover;
import JH_StackMobs.utils.StackAll;
import com.heroslender.herospawners.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;

public class JhStackMobs implements MobStackerStrategy {

    public JhStackMobs() {
        Bukkit.getLogger().info("[herospawners] JH_StackMobs foi encontrado!");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        API.setStackAmount(entity, quantidade);
        if (!StackAll.stackNear(entity, false)) {
            // New stack
            if (entity instanceof MagmaCube) {
                ((MagmaCube) entity).setSize(1);
            } else if (entity instanceof Slime) {
                ((Slime) entity).setSize(1);
            }

            if (Main.getInstace().removeIa()) {
                IaRemover.remove(entity);
            }
        }
        return false;
    }
}
