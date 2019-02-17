package com.heroslender.herospawners.events;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.spawner.ISpawner;
import com.heroslender.herospawners.spawner.Spawner;
import com.heroslender.herospawners.utils.Config;
import com.heroslender.herospawners.utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceEvent implements Listener {

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (!e.isCancelled() && e.getBlock().getType() == Material.MOB_SPAWNER) {
            if (HeroSpawners.getInstance().isShutingDown()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cNão é possivel colocar spawners quando o servidor esta a ligar/desligar.");
                return;
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(HeroSpawners.getInstance(), () -> {
                CreatureSpawner colocado = (CreatureSpawner) e.getBlock().getState();
                for (Block block : Utilities.getBlocks(e.getBlock(), Config.JUNTAR_RAIO)) {
                    if (block.getType() != Material.MOB_SPAWNER) continue;
                    CreatureSpawner cs = (CreatureSpawner) block.getState();
                    if (cs.getSpawnedType() != colocado.getSpawnedType()) continue;

                    colocado.setDelay(200);
                    colocado.update();

                    ISpawner spawner = HeroSpawners.getInstance().getStorage().getSpawner(block.getLocation());
                    if (spawner == null) continue;
                    if (spawner.getQuatidade() < Config.JUNTAR_MAX || Config.JUNTAR_MAX == 0) {
//                        spawner.incrementQuantidade();
                        spawner.setQuatidade(spawner.getQuatidade() + 1);
                        e.getBlock().setType(Material.AIR);
                        block.getWorld().spigot().playEffect(block.getLocation(), Effect.WITCH_MAGIC, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 200, 10);
//                        Bukkit.getLogger().info("Place - " + (System.currentTimeMillis() - timing2) + "ms");
                        return;
                    }
                }

                Spawner spawner = new Spawner(colocado.getLocation(), 1);
                spawner.save();
            });
        }
    }
}


