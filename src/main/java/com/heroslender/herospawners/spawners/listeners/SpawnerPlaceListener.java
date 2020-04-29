package com.heroslender.herospawners.spawners.listeners;

import com.heroslender.herospawners.spawners.SpawnerItemFactory;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class SpawnerPlaceListener implements Listener {

    @EventHandler(ignoreCancelled = false)
    private void onSpawnerPlace(final BlockPlaceEvent e) {
        if (e.getBlock().getType() != Material.MOB_SPAWNER) {
            return;
        }

        val itemEntityType = SpawnerItemFactory.getEntityType(e.getItemInHand());
        if (itemEntityType == null) {
            // Tried to place a spawner with an invalid itemStack.
            e.getPlayer().sendMessage(ChatColor.RED + "Esse spawner não é válido! Avise um membro da staff.");
            e.setCancelled(true);
            return;
        }


        val state = e.getBlock().getState();
        if (!(state instanceof CreatureSpawner)) {
            e.getPlayer().sendMessage(ChatColor.RED + "Ocurreu um erro ao colocar o spawner! " + ChatColor.GRAY + "#1");
            e.setCancelled(true);
            return;
        }

        val creatureSpawner = (CreatureSpawner) state;
        creatureSpawner.setSpawnedType(itemEntityType);
        creatureSpawner.update();
    }
}
