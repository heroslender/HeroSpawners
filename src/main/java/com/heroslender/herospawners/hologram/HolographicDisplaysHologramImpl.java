package com.heroslender.herospawners.hologram;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.heroslender.herospawners.HeroSpawners;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HolographicDisplaysHologramImpl implements Hologram {
    public static final HologramFactory FACTORY = new HolographicDisplaysHologramFactory();

    private static final class HolographicDisplaysHologramFactory implements HologramFactory {
        @Override
        public Hologram createHologram(String id, Location location, List<String> lines) {
            com.gmail.filoghost.holographicdisplays.api.Hologram hologram = HologramsAPI.createHologram(HeroSpawners.getInstance(), location);
            for (String line : lines) {
                hologram.appendTextLine(line);
            }

            return new HolographicDisplaysHologramImpl(hologram);
        }

        @Override
        public Hologram createPrivateHologram(String id, Player viewer, Location location, List<String> lines) {
            com.gmail.filoghost.holographicdisplays.api.Hologram hologram = HologramsAPI.createHologram(HeroSpawners.getInstance(), location);
            for (String line : lines) {
                hologram.appendTextLine(line);
            }

            VisibilityManager visibilityManager = hologram.getVisibilityManager();
            visibilityManager.setVisibleByDefault(false);
            visibilityManager.showTo(viewer);

            return new HolographicDisplaysHologramImpl(hologram);
        }
    }

    private final com.gmail.filoghost.holographicdisplays.api.Hologram hologram;

    public HolographicDisplaysHologramImpl(com.gmail.filoghost.holographicdisplays.api.Hologram hologram) {
        this.hologram = hologram;
    }

    @Override
    public void addLine(String line) {
        hologram.appendTextLine(line);
    }

    @Override
    public void addLine(ItemStack item) {
        hologram.appendItemLine(item);
    }

    @Override
    public void removeLine(int line) {
        if (line < size()) {
            hologram.removeLine(line);
        }
    }

    @Override
    public void setLine(int line, String text) {
        if (line < size()) {
            TextLine l = (TextLine) hologram.getLine(line);
            l.setText(text);
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
        hologram.delete();
    }
}
