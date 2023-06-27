package com.heroslender.herospawners.feature.mobstacker;

import com.heroslender.herospawners.feature.Feature;
import com.heroslender.herospawners.feature.FeatureInitializationException;
import com.heroslender.herospawners.feature.mobstacker.strategies.*;
import com.heroslender.herospawners.internal.HeroPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class MobstackerFeature extends Feature {
    public MobstackerFeature(@NotNull HeroPlugin plugin) {
        super(plugin);
    }

    @Override
    public void enableFeature() throws FeatureInitializationException {
        final MobStackerStrategy strategy;

        if (Bukkit.getPluginManager().getPlugin("MobStacker2") != null)
            strategy = new MobStacker2();
        else if (Bukkit.getPluginManager().getPlugin("StackMob") != null) {
            MobStackerStrategy strat;
            try {
                Class.forName("uk.antiperson.stackmob.api.EntityManager");
                strat = new StackMob2();
            } catch (ClassNotFoundException e) {
                try {
                    strat = new StackMob();
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    throw new FeatureInitializationException("Failed to link to StackMob", ex);
                }
            }
            strategy = strat;
        } else if (Bukkit.getPluginManager().getPlugin("TintaStack") != null)
            strategy = new TintaStack();
        else if (Bukkit.getPluginManager().getPlugin("JH_StackMobs") != null) {
            strategy = new JhStackMobs();
        } else
            strategy = new NoMobStacker();


        addListener(new SpawnerSpawnListener(strategy));
    }
}
