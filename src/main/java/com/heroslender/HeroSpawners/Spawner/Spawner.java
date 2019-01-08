package com.heroslender.herospawners.spawner;

import com.heroslender.herospawners.HeroSpawners;
import lombok.Getter;
import org.bukkit.Location;

public class Spawner implements ISpawner {
    private final Location spawnerLocation;
    @Getter private int quatidade;

    public Spawner(Location spawnerLocation, int quatidade) {
        this.spawnerLocation = spawnerLocation;
        this.quatidade = quatidade;
    }

    @Override
    public void setQuatidade(int quatidade) {
        this.quatidade = quatidade;
        save();
    }

    @Override
    public void destroy() {
        HeroSpawners.getInstance().getStorage().delSpawner(spawnerLocation);
        unload();
    }

    public void unload() {
        quatidade = -1;
    }

    public void save() {
        HeroSpawners.getInstance().getStorage().saveSpawner(spawnerLocation, getQuatidade());
        HeroSpawners.getInstance().getStorage().saveSpawnerCache(this);
    }

    public Location getSpawnerLocation() {
        return spawnerLocation.clone();
    }
}
