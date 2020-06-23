package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.StorageController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.utils.Utilities;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (e.getBlock().getType() != Material.MOB_SPAWNER
                || HeroSpawners.getInstance().shutdownCheck(e, e.getPlayer())) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(HeroSpawners.getInstance(), () -> {
            CreatureSpawner colocado = (CreatureSpawner) e.getBlock().getState();
            for (Block block : Utilities.getBlocks(e.getBlock(), config.getStackRadious())) {
                if (block.getType() != Material.MOB_SPAWNER) continue;
                CreatureSpawner cs = (CreatureSpawner) block.getState();
                if (cs.getSpawnedType() != colocado.getSpawnedType()) continue;

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

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerBreak(final BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.MOB_SPAWNER
                || HeroSpawners.getInstance().shutdownCheck(e, e.getPlayer())) {
            return;
        }

        ISpawner spawner = storageController.getSpawner(e.getBlock().getLocation());
        if (spawner == null) {
            return;
        }

        if (!spawner.getOwner().equals(e.getPlayer().getName()) && !e.getPlayer().hasPermission("herospawners.break.others")) {
            e.getPlayer().sendMessage(ChatColor.RED + "Não tens permissão para quebrar os spawners de outros players!");
            return;
        }


        if (spawner.getAmount() > 1) {
            final EntityType et = ((CreatureSpawner) e.getBlock().getState()).getSpawnedType();

            Bukkit.getScheduler().runTaskLater(HeroSpawners.getInstance(), () -> {
                e.getBlock().setType(Material.MOB_SPAWNER);
                ((CreatureSpawner) e.getBlock().getState()).setSpawnedType(et);

                spawner.setAmount(spawner.getAmount() - 1);
            }, 1L);
        } else {
            storageController.deleteSpawner(spawner);
        }
    }


    @EventHandler
    private void onSpawnerExplode(BlockExplodeEvent e) {
        if (e.getBlock().getType() == Material.MOB_SPAWNER)
            e.setCancelled(true);
    }
}
