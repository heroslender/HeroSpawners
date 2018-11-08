package com.heroslender.HeroSpawners.MobStackerSuport;

import JH_StackMobs.Main;
import com.heroslender.HeroSpawners.Spawner.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

public class JhMobStacker implements MobStackerSuport {
    private static final String META_NAME = "JH_StackMobs";

    public JhMobStacker() {
        Bukkit.getLogger().info("[HeroSpawners] Jh-MobStacker foi encontrado!");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        for (Entity e : entity.getNearbyEntities(Main.getInstace().x, Main.getInstace().y, Main.getInstace().z)) {
            if (e.getType() == entity.getType()) {
                if (e.hasMetadata(META_NAME)) {
                    quantidade = e.getMetadata(META_NAME).get(0).asInt() + quantidade;

                    e.setMetadata(META_NAME, new FixedMetadataValue(Main.getInstace(), quantidade));
                    e.setCustomName(JH_StackMobs.Main.getInstace().displayName
                            .replace("{mob}", Main.getInstace().getTranslate(entity.getType()))
                            .replace("{quantidade}", Integer.toString(quantidade)));
                    e.setCustomNameVisible(true);
                    return true;

                }
            }
        }

        entity.setMetadata(META_NAME, new FixedMetadataValue(Main.getInstace(), quantidade));
        entity.setCustomName(JH_StackMobs.Main.getInstace().displayName
                .replace("{mob}", Main.getInstace().getTranslate(entity.getType()))
                .replace("{quantidade}", Integer.toString(quantidade)));
        entity.setCustomNameVisible(true);
        return false;
    }
}
