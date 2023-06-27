package com.heroslender.herospawners.feature.chatinfo;

import com.google.common.collect.Maps;
import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class InteractListener implements Listener {
    private final ConfigurationService config;
    private final Map<Player, Long> delays = Maps.newHashMap();

    @EventHandler(ignoreCancelled = true)
    private void onInteract(PlayerInteractEvent e) {
        if ((e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK)
                || !e.hasBlock()
                || e.getClickedBlock().getType() != HeroSpawners.SPAWNER_TYPE) {
            return;
        }

        final long currTime = System.currentTimeMillis();
        final Long userDelay = delays.get(e.getPlayer());
        if (userDelay != null && currTime < userDelay) {
            // User in delay
            return;
        }

        val spawner = HeroSpawners.getInstance().getSpawnerService().getSpawner(e.getClickedBlock().getLocation());
        if (spawner == null) {
            return;
        }

        delays.put(e.getPlayer(), currTime + config.getInformationDelay());

        final String[] info = spawner.getInformationText();
        e.getPlayer().sendMessage(info);

        // Cleanup the delays map
        final Iterator<Map.Entry<Player, Long>> delaysIt = delays.entrySet().iterator();
        while (delaysIt.hasNext()) {
            final Map.Entry<Player, Long> entry = delaysIt.next();
            if (!entry.getKey().isOnline() || currTime > entry.getValue()) {
                delaysIt.remove();
            }
        }
    }
}
