package com.heroslender.herospawners.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface HologramFactory {
    Hologram createHologram(String id, Location location, List<String> lines);

    Hologram createPrivateHologram(String id, Player viewer, Location location, List<String> lines);
}
