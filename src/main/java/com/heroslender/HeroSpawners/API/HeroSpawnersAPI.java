package com.heroslender.HeroSpawners.API;

import com.heroslender.HeroSpawners.HeroSpawners;
import com.heroslender.HeroSpawners.Spawner.ISpawner;
import org.bukkit.Location;

import java.util.Map;

public class HeroSpawnersAPI {

    public static Map<Location, ISpawner> getSpawners() {
        return HeroSpawners.getInstance().getStorage().getSpawners();
    }

    public static ISpawner getSpawner(Location location){
        return HeroSpawners.getInstance().getStorage().getSpawner(location);
    }
}