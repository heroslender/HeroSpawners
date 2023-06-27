package com.heroslender.herospawners.feature.chatinfo;

import com.heroslender.herospawners.feature.Feature;
import com.heroslender.herospawners.feature.FeatureDisabledInitializationException;
import com.heroslender.herospawners.feature.FeatureInitializationException;
import com.heroslender.herospawners.internal.HeroPlugin;
import com.heroslender.herospawners.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ChatInfoFeature extends Feature {

    public ChatInfoFeature(@NotNull HeroPlugin plugin) {
        super(plugin);
    }

    @Override
    public void tryEnable() throws FeatureInitializationException {
        if (!getPlugin().getConfig().getBoolean("interact.ativar", true)) {
            throw new FeatureDisabledInitializationException("Mostrar infos ao clicar no spawner desativado!");
        }
    }

    @Override
    public void enableFeature() {
        addListener(new InteractListener(getPlugin().getService(ConfigurationService.class)));
        getPlugin().getLogger().log(Level.INFO, "Mostrar infos ao clicar no spawner ativado!");
    }
}
