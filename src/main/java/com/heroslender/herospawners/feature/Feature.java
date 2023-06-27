package com.heroslender.herospawners.feature;

import com.heroslender.herospawners.internal.HeroPlugin;
import com.heroslender.herospawners.service.Service;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class Feature implements Service {
    @Getter
    private final HeroPlugin plugin;
    private final List<Listener> featureListeners;
    @Getter private boolean enabled;

    public Feature(@NotNull final HeroPlugin plugin) {
        this.plugin = plugin;
        this.featureListeners = new ArrayList<>();
    }

    protected void addListener(Listener... listeners) {
        featureListeners.addAll(Arrays.asList(listeners));
    }

    @Override
    public final void enable() {
        try {
            tryEnable();

            enableFeature();

            plugin.registerListener(featureListeners.toArray(new Listener[0]));

            this.enabled = true;
        } catch (FeatureDisabledInitializationException e) {
            getPlugin().getLogger().log(Level.INFO, e.getMessage());
        } catch (FeatureInitializationException e) {
            if (e.getCause() != null) {
                getPlugin().getLogger().log(Level.SEVERE, e.getMessage(), e.getCause());
            } else {
                getPlugin().getLogger().log(Level.SEVERE, e.getMessage());
            }
        }
    }

    @Override
    public final void disable() {
        if (!isEnabled()) {
            return;
        }

        for (Listener featureListener : featureListeners) {
            HandlerList.unregisterAll(featureListener);
        }

        disableFeature();

        this.enabled = false;
    }

    public void tryEnable() throws FeatureInitializationException {
    }

    public void enableFeature() throws FeatureInitializationException {
    }

    public void disableFeature() {
    }
}