package com.heroslender.herospawners.mobstacker;

import com.heroslender.herospawners.models.ISpawner;
import me.leo.stack.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Plugin do LeoRamos111
 */
public class ObyStack implements MobStackerSuport {

    public ObyStack() {
        Bukkit.getLogger().info("[herospawners] ObyStack foi encontrado!");
    }

    @Override
    public boolean createOrAddStack(ISpawner spawner, Entity entity, int quantidade) {
        for (Entity e : entity.getNearbyEntities(10, 10, 10)) {
            if (e.getType() == entity.getType()) {
                if (e.hasMetadata("qnt")) {
                    quantidade = e.getMetadata("qnt").get(0).asInt() + quantidade;
                    if (quantidade >= 100000) return true;

                    e.setMetadata("qnt", new FixedMetadataValue(Main.pl, quantidade));
                    e.setCustomName("§a" + entity.getName() + " x" + quantidade);
                    return true;
                }
            }
        }
        entity.setMetadata("spawner_qnt", new FixedMetadataValue(Main.pl, spawner.getAmount()));

        entity.setMetadata("qnt", new FixedMetadataValue(Main.pl, quantidade));
        entity.setCustomName("§a" + entity.getName() + " x" + quantidade);
        entity.setCustomNameVisible(true);
        return false;
    }
}
