package com.heroslender.herospawners.spawner;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.utils.Config;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

public class Spawner implements ISpawner {
    @Getter private Location spawnerLocation;
    @Getter private int quatidade;
    private Hologram hologram;

    public Spawner(Location spawnerLocation, int quatidade) {
        this.spawnerLocation = spawnerLocation;
        this.quatidade = quatidade;
        reloadHologram();
    }

    @Override
    public void reloadHologram() {
        if (!Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().scheduleSyncDelayedTask(HeroSpawners.getInstance(), this::reloadHologram);
            return;
        }
        if (hologram == null || hologram.isDeleted()) {
            hologram = HologramsAPI.createHologram(HeroSpawners.getInstance(), spawnerLocation.clone().add(0.5, 1.4, 0.5));
            hologram.appendTextLine(Config.TEXTO_HOLOGRAMA
                    .replace("%quantidade%", getQuatidade() + "")
                    .replace("%tipo%", Config.getNomeEntidade(((CreatureSpawner) spawnerLocation.getBlock().getState()).getSpawnedType())));
        }
        hologram.appendTextLine(Config.TEXTO_HOLOGRAMA
                .replace("%quantidade%", getQuatidade() + "")
                .replace("%tipo%", Config.getNomeEntidade(((CreatureSpawner) spawnerLocation.getBlock().getState()).getSpawnedType())));
        if (hologram.size() > 1)
            hologram.removeLine(0);
    }

    @Override
    public void setQuatidade(int quatidade) {
        this.quatidade = quatidade;
        save();
        reloadHologram();
    }

    @Override
    public void destroy() {
        hologram.delete();
        hologram = null;
        HeroSpawners.getInstance().getStorage().delSpawner(spawnerLocation);
        spawnerLocation = null;
    }

    public void unload() {
        hologram.delete();
        hologram = null;
        spawnerLocation = null;
    }

    public void save(){
        HeroSpawners.getInstance().getStorage().saveSpawner(spawnerLocation, getQuatidade());
        HeroSpawners.getInstance().getStorage().saveSpawnerCache(this);
    }
}
