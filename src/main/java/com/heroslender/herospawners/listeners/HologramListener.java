package com.heroslender.herospawners.listeners;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class HologramListener implements Listener {
    private final ConfigurationController config;
    private final List<Player> viewers = new ArrayList<>();

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent e) {
        if (e.isCancelled() || viewers.contains(e.getPlayer()))
            return;

        try {
            val target = e.getPlayer().getTargetBlock(Collections.singleton(Material.AIR), config.getHologramViewDistance());

            if (target.getType() == Material.MOB_SPAWNER) {
                val spawner = (Spawner) HeroSpawners.getInstance().getStorageController().getSpawner(target.getLocation());

                if (spawner != null) {
                    setSpawnerHologram(e.getPlayer(), spawner);
                }
            }
        } catch (IllegalStateException ignore) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSpawnerHologram(final Player player, final ISpawner spawner) {
        val blockState = spawner.getLocation().getBlock().getState();
        if (!(blockState instanceof CreatureSpawner))
            return;

        val hologramHeight = config.isHologramShowHead() ? 1.9 : 1.4;
        val hologramInfo = HologramsAPI.createHologram(HeroSpawners.getInstance(), spawner.getLocation().add(0.5, hologramHeight, 0.5));
        hologramInfo.getVisibilityManager().setVisibleByDefault(false);
        hologramInfo.getVisibilityManager().showTo(player);
        val entityType = ((CreatureSpawner) blockState).getSpawnedType();
        val entityProperties = config.getProperties(entityType);
        val linha = hologramInfo.appendTextLine(config.getHologramText()
                .replace("%quantidade%", spawner.getAmount() + "")
                .replace("%tipo%", entityProperties.getDisplayName()));
        if (config.isHologramShowHead()) {
            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            val skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwner(entityProperties.getSkullSkinName());
            head.setItemMeta(skullMeta);
            hologramInfo.appendItemLine(head);
        }
        viewers.add(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()
                        || !viewers.contains(player)
                        || spawner.getAmount() < 0
                        || !spawner.getLocation().equals(player.getTargetBlock(Collections.singleton(Material.AIR), config.getHologramViewDistance()).getLocation())) {
                    cancel();
                    hologramInfo.delete();
                    viewers.remove(player);
                    return;
                }

                val novaLinha = config.getHologramText()
                        .replace("%quantidade%", spawner.getAmount() + "")
                        .replace("%tipo%", entityProperties.getDisplayName());
                if (!novaLinha.equals(linha.getText())) {
                    linha.setText(novaLinha);
                }
            }
        }.runTaskTimer(HeroSpawners.getInstance(), 5L, 5L);
    }
}
