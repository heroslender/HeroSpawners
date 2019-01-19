package com.heroslender.herospawners.events;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.spawner.Spawner;
import com.heroslender.herospawners.utils.Config;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HologramListener implements Listener {
    private final List<Player> viewers = new ArrayList<>();

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent e) {
        if (e.isCancelled() || viewers.contains(e.getPlayer()))
            return;

        try {
            val target = e.getPlayer().getTargetBlock(Collections.singleton(Material.AIR), Config.SPAWNER_HOLOGRAM_VIEW_DISTANCE);

            if (target.getType() == Material.MOB_SPAWNER) {
                val spawner = (Spawner) HeroSpawners.getInstance().getStorage().getSpawner(target.getLocation());

                if (spawner != null) {
                    setSpawnerHologram(e.getPlayer(), spawner);
                }
            }
        } catch (IllegalStateException ignore) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSpawnerHologram(final Player player, final Spawner spawner) {
        val blockState = spawner.getSpawnerLocation().getBlock().getState();
        if (!(blockState instanceof CreatureSpawner))
            return;

        val hologramInfo = HologramsAPI.createHologram(HeroSpawners.getInstance(), spawner.getSpawnerLocation().add(0.5, 1.4, 0.5));
        hologramInfo.getVisibilityManager().setVisibleByDefault(false);
        hologramInfo.getVisibilityManager().showTo(player);

        val nomeEntidade = Config.getNomeEntidade(((CreatureSpawner) blockState).getSpawnedType());
        val linha = hologramInfo.appendTextLine(Config.TEXTO_HOLOGRAMA
                .replace("%quantidade%", spawner.getQuatidade() + "")
                .replace("%tipo%", nomeEntidade));
        viewers.add(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()
                        || !viewers.contains(player)
                        || spawner.getQuatidade() < 0
                        || !spawner.getSpawnerLocation().equals(player.getTargetBlock(Collections.singleton(Material.AIR), Config.SPAWNER_HOLOGRAM_VIEW_DISTANCE).getLocation())) {
                    cancel();
                    hologramInfo.delete();
                    viewers.remove(player);
                    return;
                }

                val novaLinha = Config.TEXTO_HOLOGRAMA
                        .replace("%quantidade%", spawner.getQuatidade() + "")
                        .replace("%tipo%", nomeEntidade);
                if (!novaLinha.equals(linha.getText())) {
                    linha.setText(novaLinha);
                }
            }
        }.runTaskTimer(HeroSpawners.getInstance(), 5L, 5L);
    }
}
