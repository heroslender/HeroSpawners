package com.heroslender.herospawners.feature.mobstacker.strategies;

import JH_StackMobs.API;
import JH_StackMobs.Main;
import JH_StackMobs.utils.IaRemover;
import com.heroslender.herospawners.feature.mobstacker.MobStackerStrategy;
import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;

import java.util.List;

public class JhStackMobs implements MobStackerStrategy {

    public JhStackMobs() {
        Bukkit.getLogger().info("[herospawners] JH_StackMobs foi encontrado!");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        int limite = Main.getInstace().iiIIiIIIii;

        if (!Main.getInstance().getConfig().getBoolean("Stack.NovoMob")) {
            List<Entity> nearbyEntities = entity.getNearbyEntities(Main.getInstace().IiIIIIIiii, Main.getInstace().iIiiiiiiii, Main.getInstace().iiIIiiiIIi);
            for (Entity nearbyEntity : nearbyEntities) {
                if (nearbyEntity.isValid() && nearbyEntity != entity && !nearbyEntity.equals(entity)) {
                    if (nearbyEntity.getType() != entity.getType()) {
                        continue;
                    }

                    int stackAmount = API.getStackAmount(nearbyEntity);
                    if (stackAmount >= limite) {
                        return true;
                    }

                    setStackAmount(nearbyEntity, Math.min(stackAmount + quantidade, limite));
                    return true;
                }
            }

            Entity e = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
            setStackAmount(e, Math.min(quantidade, limite));
            return true;
        }

        for (int i = 0; i < quantidade / limite; i++) {
            Entity e = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
            setStackAmount(e, limite);
        }

        Entity e = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        setStackAmount(e, quantidade % limite);
        return true;
    }

    private void setStackAmount(Entity entity, int amount) {
        API.setStackAmount(entity, amount);

        if (entity instanceof MagmaCube) {
            ((MagmaCube) entity).setSize(1);
        } else if (entity instanceof Slime) {
            ((Slime) entity).setSize(1);
        }

        if (Main.getInstace().removeIa()) {
            IaRemover.remove(entity);
        }
    }
}
