package com.heroslender.herospawners.models;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.utils.Utilities;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

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

    @Override
    public CreatureSpawner getState() {
        return (CreatureSpawner) getLocation().getBlock().getState();
    }

    @Override
    public EntityType getType() {
        return getState().getSpawnedType();
    }

    @Override
    public String toString() {
        return "Spawner(location=\"" + Utilities.loc2str(getLocation()) + "\", quantidade=\"" + getAmount() + "\")";
    }
}
