package com.heroslender.herospawners.listeners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.logging.Level;

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
            // Exception thrown by bukkit if the player looks at the void for example
        } catch (Exception ex) {
            HeroSpawners.getInstance().getLogger().log(Level.SEVERE, "Ocurreu um erro.", ex);
        }
    }

    private void setSpawnerHologram(final Player player, final ISpawner spawner) {
        val loc = spawner.getLocation().add(0.5, 1.4, 0.5);
        if (config.isHologramShowHead()) {
            loc.add(0, .5, 0);
        }
        val hologram = createHologramFor(player, loc);

        val entityProperties = config.getProperties(spawner.getType());
        val linha = hologram.appendTextLine(config.getHologramText()
                .replace("%dono%", spawner.getOwner())
                .replace("%quantidade%", Integer.toString(spawner.getAmount()))
                .replace("%tipo%", entityProperties.getDisplayName()));
        if (config.isHologramShowHead()) {
            hologram.appendItemLine(getSkull(entityProperties.getSkullSkinName()));
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
                    hologram.delete();
                    viewers.remove(player);
                    return;
                }

                val novaLinha = config.getHologramText()
                        .replace("%dono%", spawner.getOwner())
                        .replace("%quantidade%", Integer.toString(spawner.getAmount()))
                        .replace("%tipo%", entityProperties.getDisplayName());
                if (!novaLinha.equals(linha.getText())) {
                    linha.setText(novaLinha);
                }
            }
        }.runTaskTimer(HeroSpawners.getInstance(), 5L, 5L);
    }

    private Hologram createHologramFor(final Player player, final Location location) {
        val hologram = HologramsAPI.createHologram(HeroSpawners.getInstance(), location);
        hologram.getVisibilityManager().setVisibleByDefault(false);
        hologram.getVisibilityManager().showTo(player);
        return hologram;
    }

    private ItemStack getSkull(final String owner) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        val skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwner(owner);
        head.setItemMeta(skullMeta);
        return head;
    }
}
