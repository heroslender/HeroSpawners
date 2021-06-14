package com.heroslender.herospawners.utils;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.SpawnerController;
import com.heroslender.herospawners.models.ISpawner;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.*;
import java.util.logging.Level;

import static com.heroslender.herospawners.controllers.ConfigurationController.SKULL_PLACEHOLDER;

public class HologramFacade {
    protected static final Set<Material> transparentBlocks = Collections.singleton(Material.AIR);
    public static final Material SKULL_MATERIAL;

    static {
        Material mat;
        try {
            mat = Material.valueOf("PLAYER_HEAD");
            System.out.println(mat);
        } catch (IllegalArgumentException e) {
            mat = Material.SKULL_ITEM;
        }

        SKULL_MATERIAL = mat;
    }

    protected final ConfigurationController config;
    protected final SpawnerController spawnerController;
    protected final double hologramOffset;
    protected final List<Player> viewers = new ArrayList<>();

    public HologramFacade(ConfigurationController config, SpawnerController storage) {
        this.config = config;
        this.spawnerController = storage;

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

    protected void setHologram(final Player player) {
        if (viewers.contains(player)) {
            return;
        }

        try {
            val spawnerBlock = getTargetSpawner(player);
            if (spawnerBlock == null) {
                return;
            }

            val spawner = spawnerController.getSpawner(spawnerBlock.getLocation());
            if (spawner == null) {
                return;
            }

            setSpawnerHologram(player, spawner);
        } catch (IllegalStateException ignore) {
            // Exception thrown by bukkit if the player looks at the void for example
        } catch (Exception ex) {
            HeroSpawners.getInstance().getLogger().log(Level.SEVERE, "Ocurreu um erro.", ex);
        }
    }

    private Block getTargetSpawner(final Player player) {
        BlockIterator itr = new BlockIterator(player, config.getHologramViewDistance());

        Block block = null;
        try {
            while (true) {
                val nextBlock = itr.next();
                if (nextBlock.getType() != Material.AIR) {
                    block = nextBlock;
                    break;
                }
            }
        } catch (NoSuchElementException e) {
            // BlockIterator has no more elements
        }

        if (block != null && block.getType() != HeroSpawners.SPAWNER_TYPE) {
            return null;
        }

        return block;
    }

    private void setSpawnerHologram(final Player player, final ISpawner spawner) {
        val loc = spawner.getLocation().add(
            config.getHologramOffsetX(),
            config.getHologramOffsetY() + hologramOffset,
            config.getHologramOffsetZ()
        );
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
        ItemStack head = new ItemStack(SKULL_MATERIAL, 1, (short) 3);
        val skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwner(owner);
        head.setItemMeta(skullMeta);
        return head;
    }
}
