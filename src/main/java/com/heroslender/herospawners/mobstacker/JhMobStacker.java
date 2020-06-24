package com.heroslender.herospawners.mobstacker;

import JH_StackMobs.API;
import JH_StackMobs.Main;
import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import ultils.IaRemover;
import ultils.StackAll;

public class JhMobStacker implements MobStackerSupport {

    public JhMobStacker() {
        Bukkit.getLogger().info("[herospawners] Jh-MobStacker foi encontrado!");
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
