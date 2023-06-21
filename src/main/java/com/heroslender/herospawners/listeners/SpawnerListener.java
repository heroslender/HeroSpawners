package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.SpawnerController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.utils.Utilities;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class SpawnerListener implements Listener {
    @Getter(AccessLevel.PRIVATE) private final Logger logger = HeroSpawners.getInstance().getLogger();
    private final ConfigurationController config;
    private final SpawnerController spawnerController;

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (e.getBlock().getType() != HeroSpawners.SPAWNER_TYPE
            || HeroSpawners.getInstance().shutdownCheck(e, e.getPlayer())) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(HeroSpawners.getInstance(), () -> {
            CreatureSpawner creatureSpawner = (CreatureSpawner) e.getBlock().getState();
            for (Block block : Utilities.getBlocks(e.getBlock(), config.getStackRadious())) {
                if (block.getType() != HeroSpawners.SPAWNER_TYPE) {
                    continue;
                }

                CreatureSpawner cs = (CreatureSpawner) block.getState();
                if (cs.getSpawnedType() != creatureSpawner.getSpawnedType()) {
                    continue;
                }

                ISpawner spawner = spawnerController.getSpawner(block.getLocation());
                if (spawner == null) {
                    continue;
                }

                if (spawner.getAmount() < config.getStackLimit() || config.getStackLimit() == 0) {
                    spawnerController.updateSpawner(e.getPlayer(), spawner, spawner.getAmount() + 1);

                    e.getBlock().setType(Material.AIR);
                    return;
                }
            }

            spawnerController.saveSpawner(e.getPlayer(), creatureSpawner, 1);
        });
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerBreak(final BlockBreakEvent e) {
        if (e.getBlock().getType() != HeroSpawners.SPAWNER_TYPE
            || HeroSpawners.getInstance().shutdownCheck(e, e.getPlayer())) {
            return;
        }

        ISpawner spawner = spawnerController.getSpawner(e.getBlock().getLocation());
        if (spawner == null) {
            return;
        }

        if (!spawner.getOwner().equals(e.getPlayer().getName()) && !e.getPlayer().hasPermission("herospawners.break.others")) {
            e.getPlayer().sendMessage(ChatColor.RED + "Não tens permissão para quebrar os spawners de outros players!");
            e.setCancelled(true);
            return;
        }

        if (config.isRequirePlotTrust()
            && !e.getPlayer().hasPermission("herospawners.admin")
            && PS.get().hasPlotArea(spawner.getLocation().getWorld().getName())) {
            org.bukkit.Location loc = spawner.getLocation();
            Location plotLoc = new Location(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
            );

            Plot plot = plotLoc.getOwnedPlot();
            if (plot != null && !plot.getTrusted().contains(e.getPlayer().getUniqueId())) {
                e.getPlayer().sendMessage(ChatColor.RED + "Não tens permissão para quebrar os spawners desse plot! Apenas jogadores com trust.");
                e.setCancelled(true);
                return;
            }
        }

        spawnerController.updateSpawner(e.getPlayer(), spawner, spawner.getAmount() - 1);

        if (spawner.getAmount() >= 1) {
            final CreatureSpawner creatureSpawner = (CreatureSpawner) e.getBlock().getState();
            final EntityType et = (creatureSpawner).getSpawnedType();

            Bukkit.getScheduler().runTaskLater(HeroSpawners.getInstance(), () -> {
                e.getBlock().setType(HeroSpawners.SPAWNER_TYPE);
                creatureSpawner.setSpawnedType(et);
                // Reset the spawn delay
                creatureSpawner.setDelay(200 + Utilities.getRandom().nextInt(600));
            }, 1L);
        }
    }


    @EventHandler
    private void onSpawnerExplode(BlockExplodeEvent e) {
        if (e.getBlock().getType() == HeroSpawners.SPAWNER_TYPE) {
            e.setCancelled(true);
        }
    }
}
