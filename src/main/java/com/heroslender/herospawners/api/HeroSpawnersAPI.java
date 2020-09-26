package com.heroslender.herospawners.api;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.ISpawner;
import org.bukkit.Location;

public class HeroSpawnersAPI {

    public static ISpawner getSpawner(Location location){
        return HeroSpawners.getInstance().getSpawnerController().getSpawner(location);
    }
}