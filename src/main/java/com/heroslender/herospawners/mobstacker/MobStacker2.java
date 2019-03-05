package com.heroslender.herospawners.mobstacker;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.ISpawner;
import com.kiwifisher.mobstacker2.io.Settings;
import com.kiwifisher.mobstacker2.metadata.MetaTags;
import com.kiwifisher.mobstacker2.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Heroslender.
 */
public class MobStacker2 implements MobStackerSuport {

    public MobStacker2() {
        Bukkit.getLogger().info("[herospawners] MobStacker foi encontrado!");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity1, int quantidade) {
        for (Entity entity : entity1.getNearbyEntities(3D, 3D, 3D)) {
            if (entity.getType() == entity1.getType()) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (Settings.STACKING_TYPES.contains(livingEntity.getType().name()) && Settings.SPAWN_TYPES.contains(CreatureSpawnEvent.SpawnReason.SPAWNER.name())) {
                    if (Settings.MAX_PER_CHUNK.containsKey(livingEntity.getType().name()) && Util.quantityInChunk(livingEntity) >= Settings.MAX_PER_CHUNK.get(livingEntity.getType().name())) {
                        return true;
                    }

                    if (Settings.BLACKLISTED_WORLDS.contains(entity.getWorld().getName())) {
                        return true;
                    }
                    if (MetaTags.hasMetaData(livingEntity)) {
                        MetaTags.setQuantity(livingEntity, MetaTags.getQuantity(livingEntity) + quantidade);
//                        Bukkit.getLogger().info("stack setado! " + MetaTags.getQuantity(livingEntity) + " - " + quantidade);
                        return true;
                    }

                    MetaTags.setQuantity(livingEntity, quantidade);
                    MetaTags.setSpawnReason(livingEntity, CreatureSpawnEvent.SpawnReason.SPAWNER);
                    MetaTags.setStacking(livingEntity, true);
                    MetaTags.updateName(livingEntity);
//                    Bukkit.getLogger().info("Setando stack " + MetaTags.getQuantity(livingEntity));
                    return true;
                }
            }
        }
        if (Settings.STACKING_TYPES.contains(entity1.getType().name()) && Settings.SPAWN_TYPES.contains(CreatureSpawnEvent.SpawnReason.SPAWNER.name())) {
            LivingEntity livingEntity = (LivingEntity) entity1;
            if (Settings.MAX_PER_CHUNK.containsKey(entity1.getType().name()) && Util.quantityInChunk(livingEntity) >= Settings.MAX_PER_CHUNK.get(livingEntity.getType().name())) {
                return true;
            }

            if (Settings.BLACKLISTED_WORLDS.contains(entity1.getWorld().getName())) {
                return true;
            }
            if (MetaTags.hasMetaData(livingEntity)) {
                MetaTags.setQuantity(livingEntity, MetaTags.getQuantity(livingEntity) + quantidade);
//                Bukkit.getLogger().info("stack setado! " + MetaTags.getQuantity(livingEntity) + " - " + quantidade);
                return false;
            }

            (new BukkitRunnable() {
                public void run() {
                    MetaTags.setQuantity(livingEntity, quantidade);
                    MetaTags.setSpawnReason(livingEntity, CreatureSpawnEvent.SpawnReason.SPAWNER);
                    MetaTags.setStacking(livingEntity, true);
                    MetaTags.updateName(livingEntity);
//                    Bukkit.getLogger().info("Setando stack " + MetaTags.getQuantity(livingEntity));
                }
            }).runTaskLater(HeroSpawners.getInstance(), 5L);
        }
        return false;
    }
}
