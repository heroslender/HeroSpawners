package com.heroslender.herospawners.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DecentHologramsHologramImpl implements Hologram {
    public static final HologramFactory FACTORY = new DecentHologramsHologramFactory();

    private static class DecentHologramsHologramFactory implements HologramFactory {
        @Override
        public Hologram createHologram(String id, Location location, List<String> lines) {
            eu.decentsoftware.holograms.api.holograms.Hologram hologram =
                DHAPI.createHologram(id, location, false, lines);

            return new DecentHologramsHologramImpl(hologram);
        }

        @Override
        public Hologram createPrivateHologram(String id, Player viewer, Location location, List<String> lines) {
            if (eu.decentsoftware.holograms.api.holograms.Hologram.getCachedHologramNames().contains(id)) {
                throw new IllegalArgumentException(String.format("Hologram with that name already exists! (%s)", id));
            }

            eu.decentsoftware.holograms.api.holograms.Hologram hologram = new eu.decentsoftware.holograms.api.holograms.Hologram(id, location, false) {
                @Override
                public boolean canShow(Player player) {
                    return player.equals(viewer);
                }
            };

            HologramPage page = hologram.getPage(0);
            for (String line : lines) {
                HologramLine hologramLine = new HologramLine(page, page.getNextLineLocation(), line);
                page.addLine(hologramLine);
            }

            hologram.showAll();

            return new DecentHologramsHologramImpl(hologram);
        }
    }

    private final eu.decentsoftware.holograms.api.holograms.Hologram hologram;

    public DecentHologramsHologramImpl(eu.decentsoftware.holograms.api.holograms.Hologram hologram) {
        this.hologram = hologram;
    }

    @Override
    public void addLine(String line) {
        DHAPI.addHologramLine(hologram, line);
    }

    @Override
    public void addLine(ItemStack item) {
    }

    @Override
    public void removeLine(int line) {
        if (line < size()) {
            DHAPI.removeHologramLine(hologram, line);
        }
    }

    @Override
    public void setLine(int line, String text) {
        if (line < size()) {
            DHAPI.setHologramLine(hologram, line, text);
        } else {
            addLine(text);
        }
    }

    @Override
    public int size() {
        return hologram.getPage(0).size();
    }

    @Override
    public void remove() {
        DHAPI.removeHologram(hologram.getId());
    }
}
