package com.heroslender.herospawners.listeners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.models.ISpawner;
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
import java.util.Set;
import java.util.logging.Level;

import static com.heroslender.herospawners.controllers.ConfigurationController.SKULL_PLACEHOLDER;

@RequiredArgsConstructor
public class HologramListener implements Listener {
    private static final Set<Material> transparentBlocks = Collections.singleton(Material.AIR);

    private final ConfigurationController config;
    private final List<Player> viewers = new ArrayList<>();
    private final double hologramOffset;

    public HologramListener(ConfigurationController config) {
        this.config = config;

        val holoLines = config.getHologramText();
        double offset = 0D;
        for (String line : holoLines) {
            if (line.equalsIgnoreCase(SKULL_PLACEHOLDER)) {
                // Offset for items
                offset += 0.7;
            } else {
                // Offset for text lines
                offset += 0.23;
            }
        }

        if (config.trimHologram()) {
            // Last line is an item, place hologram lower
            offset -= 0.2;
        }

        this.hologramOffset = offset;
    }

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent e) {
        if (e.isCancelled() || viewers.contains(e.getPlayer()))
            return;

        try {
            val target = e.getPlayer().getTargetBlock(transparentBlocks, config.getHologramViewDistance());

            if (target.getType() == Material.MOB_SPAWNER) {
                val spawner = HeroSpawners.getInstance().getStorageController().getSpawner(target.getLocation());

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
        val loc = spawner.getLocation().add(0.5, 1.17 + hologramOffset, 0.5);
        val hologram = createHologramFor(player, loc);

        val lines = new ArrayList<HologramLine>();
        for (String line : spawner.getHologramText()) {
            if (line.equalsIgnoreCase(SKULL_PLACEHOLDER)) {
                String spawnerSkullName = spawner.getEntityProperties().getSkullSkinName();
                lines.add(hologram.appendItemLine(getSkull(spawnerSkullName)));
            } else {
                lines.add(hologram.appendTextLine(line));
            }
        }

        viewers.add(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()
                        || !viewers.contains(player)
                        || spawner.getAmount() < 0
                        || !spawner.getLocation().equals(player.getTargetBlock(transparentBlocks, config.getHologramViewDistance()).getLocation())) {
                    cancel();
                    hologram.delete();
                    viewers.remove(player);
                    return;
                }

                // Update the hologram if needed
                val newLines = spawner.getHologramText();
                for (int i = 0; i < newLines.size(); i++) {
                    val currentLine = lines.get(i);
                    if (currentLine instanceof TextLine) {
                        TextLine textLine = (TextLine) currentLine;
                        String newValue = newLines.get(i);

                        if (!textLine.getText().equals(newValue)) {
                            textLine.setText(newValue);
                        }
                    }
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
