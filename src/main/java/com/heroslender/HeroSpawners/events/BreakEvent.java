package com.heroslender.herospawners.events;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.spawner.ISpawner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakEvent implements org.bukkit.event.Listener {
//    private herospawners ss;

//    public BreakEvent(herospawners ss) {
//        this.ss = ss;
//    }

    @EventHandler
    private void onSpawnerBreak(final BlockBreakEvent e) {
        if ((!e.isCancelled()) && e.getBlock().getType() == Material.MOB_SPAWNER) {
            ISpawner spawner = HeroSpawners.getInstance().getStorage().getSpawner(e.getBlock().getLocation());
            if (spawner == null)
                return;

            if (HeroSpawners.getInstance().isShutingDown()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cNão é possivel quebrar spawners quando o servidor esta a ligar/desligar.");
                return;
            }

            if (spawner.getQuatidade() > 1) {
                final EntityType et = ((CreatureSpawner) e.getBlock().getState()).getSpawnedType();

                Bukkit.getScheduler().runTaskLater(HeroSpawners.getInstance(), () -> {
                    e.getBlock().setType(Material.MOB_SPAWNER);
                    ((CreatureSpawner) e.getBlock().getState()).setSpawnedType(et);
                    ((CreatureSpawner) e.getBlock().getState()).setDelay(200);
                    e.getBlock().getState().update();

                    spawner.setQuatidade(spawner.getQuatidade() - 1);
                    HeroSpawners.getInstance().newSpawner.add(e.getBlock().getLocation());
                }, 1L);
            } else {
                spawner.destroy();
            }

//            if (ss.spawnerAmount.get(e.getBlock().getLocation()) > 1) {
//                final EntityType et = ((CreatureSpawner) e.getBlock().getState()).getSpawnedType();
//                this.ss.getServer().getScheduler().runTaskLater(this.ss, () -> {
//                    e.getBlock().setType(Material.MOB_SPAWNER);
//                    ((CreatureSpawner) e.getBlock().getState()).setSpawnedType(et);
//                    ((CreatureSpawner) e.getBlock().getState()).setDelay(200);
//                    e.getBlock().getState().update();
//                    ss.spawnerAmount.put(e.getBlock().getLocation(), ss.spawnerAmount.get(e.getBlock().getLocation()) - 1);
//                    ss.newSpawner.add(e.getBlock().getLocation());
//                    ss.util.atualizaHolograma(e.getBlock().getLocation());
//                    ss.sa.saveSpawner(e.getBlock().getLocation(), ss.spawnerAmount.get(e.getBlock().getLocation()));
////                        ss.util.updateTag((ArmorStand) ss.util.getArmorStand(e.getBlock()));
//                }, 1L);
//            } else {
////                this.ss.util.getArmorStand(e.getBlock()).remove();
//                ss.util.apagaHolograma(e.getBlock().getLocation().clone());
//                this.ss.spawnerAmount.remove(e.getBlock().getLocation());
//                ss.sa.saveSpawner(e.getBlock().getLocation(), 0);
//            }
        }
    }
}


