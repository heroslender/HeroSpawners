package com.heroslender.herospawners.feature.hologram;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.feature.Feature;
import com.heroslender.herospawners.feature.FeatureDisabledInitializationException;
import com.heroslender.herospawners.feature.FeatureInitializationException;
import com.heroslender.herospawners.feature.hologram.HologramFactory;
import com.heroslender.herospawners.feature.hologram.impl.DecentHologramsHologramImpl;
import com.heroslender.herospawners.feature.hologram.impl.HolographicDisplaysHologramImpl;
import com.heroslender.herospawners.feature.hologram.strategy.HologramDisplayStrategy;
import com.heroslender.herospawners.feature.hologram.strategy.PlayerMoveHologramDisplayStategy;
import com.heroslender.herospawners.feature.hologram.strategy.TimerHologramDisplayStrategy;
import com.heroslender.herospawners.internal.HeroPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Level;

public class HologramFeature extends Feature {
    @Getter HologramFactory hologramFactory;
    private HologramDisplayStrategy displayStrategy;

    public HologramFeature(@NotNull HeroSpawners plugin) {
        super(plugin);
    }

    @Override
    public void tryEnable() throws FeatureInitializationException {
        if (!getPlugin().getConfig().getBoolean("holograma.ativar", true)) {
            throw new FeatureDisabledInitializationException("Hologramas nos spawners desativado!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            this.hologramFactory = HolographicDisplaysHologramImpl.FACTORY;
            getPlugin().getLogger().info("HolographicDisplays detetado! Ativando hologramas nos spawners.");
        } else if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            this.hologramFactory = DecentHologramsHologramImpl.FACTORY;
            getPlugin().getLogger().info("DecentHolograms detetado! Ativando hologramas nos spawners.");
        } else {
            this.hologramFactory = null;
            throw new FeatureDisabledInitializationException("Nenhum plugin de holograma suportado foi detetado! Desativado hologramas nos spawners.");
        }
    }

    @Override
    public void enableFeature() {
        HeroPlugin plugin = getPlugin();
        switch (plugin.getConfig().getString("holograma.metodo", "TIMER").toUpperCase(Locale.ROOT)) {
            case "TIMER":
                this.displayStrategy = new TimerHologramDisplayStrategy(plugin, getHologramFactory());
                break;
            case "MOVE_EVENT":
                this.displayStrategy = new PlayerMoveHologramDisplayStategy(plugin, getHologramFactory());
                break;
        }

        displayStrategy.enable();
    }

    @Override
    public void disableFeature() {
        displayStrategy.disable();
    }
}
