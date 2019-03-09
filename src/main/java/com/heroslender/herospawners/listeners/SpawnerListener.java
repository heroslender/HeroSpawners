package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.StorageController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.utils.Utilities;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;

@RequiredArgsConstructor
public class SpawnerListener implements Listener {
    private final ConfigurationController config;
    private final StorageController storageController;

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
                for (Block block : Utilities.getBlocks(e.getBlock(), config.getStackRadious())) {
                    if (block.getType() != Material.MOB_SPAWNER) continue;
                    CreatureSpawner cs = (CreatureSpawner) block.getState();
                    if (cs.getSpawnedType() != colocado.getSpawnedType()) continue;

                    colocado.setDelay(200);
                    colocado.update();

                    ISpawner spawner = storageController.getSpawner(block.getLocation());
                    if (spawner == null) continue;
                    if (spawner.getAmount() < config.getStackLimit() || config.getStackLimit() == 0) {
                        spawner.setAmount(spawner.getAmount() + 1);
                        e.getBlock().setType(Material.AIR);
                        block.getWorld().spigot().playEffect(block.getLocation(), Effect.WITCH_MAGIC, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 200, 10);
                        return;
                    }
                }

                Spawner spawner = new Spawner(e.getPlayer().getName(), colocado.getLocation(), 1);
                storageController.saveSpawner(spawner);
            });
        }
    }

    @EventHandler
    private void onSpawnerBreak(final BlockBreakEvent e) {
        if ((!e.isCancelled()) && e.getBlock().getType() == Material.MOB_SPAWNER) {
            ISpawner spawner = storageController.getSpawner(e.getBlock().getLocation());
            if (spawner == null)
                return;

            if (HeroSpawners.getInstance().isShutingDown()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cNão é possivel quebrar spawners quando o servidor esta a ligar/desligar.");
                return;
            }

            if (spawner.getAmount() > 1) {
                final EntityType et = ((CreatureSpawner) e.getBlock().getState()).getSpawnedType();

                Bukkit.getScheduler().runTaskLater(HeroSpawners.getInstance(), () -> {
                    e.getBlock().setType(Material.MOB_SPAWNER);
                    ((CreatureSpawner) e.getBlock().getState()).setSpawnedType(et);
                    ((CreatureSpawner) e.getBlock().getState()).setDelay(200);
                    e.getBlock().getState().update();

                    spawner.setAmount(spawner.getAmount() - 1);
                    HeroSpawners.getInstance().newSpawner.add(e.getBlock().getLocation());
                }, 1L);
            } else {
                storageController.deleteSpawner(spawner);
            }
        }
    }


    @EventHandler
    private void onSpawnerExplode(BlockExplodeEvent e) {
        if (e.getBlock().getType() == Material.MOB_SPAWNER)
            e.setCancelled(true);
    }
}
