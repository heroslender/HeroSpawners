package com.heroslender.herospawners.models;

import com.heroslender.herospawners.HeroSpawners;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@AllArgsConstructor
public class Spawner implements ISpawner {
    @Getter private final String owner;
    private final Location location;
    @Getter private int amount;

    public void setAmount(int amount) {
        this.amount = amount;
        HeroSpawners.getInstance().getStorageController().updateSpawner(this);
    }

    public Location getLocation() {
        return location.clone();
    }
}
