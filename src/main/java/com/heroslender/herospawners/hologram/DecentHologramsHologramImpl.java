package com.heroslender.herospawners.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
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
            eu.decentsoftware.holograms.api.holograms.Hologram hologram =
                DHAPI.createHologram(id, location, false, lines);

            hologram.hideAll();
            hologram.show(viewer, hologram.getPlayerPage(viewer));

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
        return hologram.size();
    }

    @Override
    public void remove() {
        DHAPI.removeHologram(hologram.getId());
    }
}
