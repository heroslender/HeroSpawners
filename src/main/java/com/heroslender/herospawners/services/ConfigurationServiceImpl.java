package com.heroslender.herospawners.services;

import com.heroslender.herospawners.HeroSpawners;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class ConfigurationServiceImpl implements ConfigurationService {

    @Override
    public void init() {
        HeroSpawners.getInstance().saveDefaultConfig();
        HeroSpawners.getInstance().reloadConfig();
        final FileConfiguration configuration = HeroSpawners.getInstance().getConfig();

        if (!configuration.isSet("juntar.maximo"))
            configuration.set("juntar.maximo", 0);
        if (!configuration.isSet("juntar.raio"))
            configuration.set("juntar.raio", 5);

        if (!configuration.isSet("msg.colocar"))
            configuration.set("msg.colocar", "Colocaste +{quantidade} spawners, ficando assim com {spawner_quantidade}!");
        if (!configuration.isSet("msg.quebrar"))
            configuration.set("msg.quebrar", "Quebraste {quantidade} spawners, ficando assim com {spawner_quantidade}!");

        for (EntityType e : EntityType.values()) {
            if (e.getEntityClass() != null
                    && LivingEntity.class.isAssignableFrom(e.getEntityClass())) {
                if (!configuration.contains("mobs." + e.name() + ".name")) {
                    configuration.set("mobs." + e.name() + ".name", e.getName());
                }
                if (!configuration.contains("mobs." + e.name() + ".head")) {
                    configuration.set("mobs." + e.name() + ".head", "MHF_" + e.getName());
                }
            }
        }

        HeroSpawners.getInstance().saveConfig();
    }

    @Override
    public FileConfiguration getConfig() {
        return HeroSpawners.getInstance().getConfig();
    }

    @Override
    public void stop() {

    }
}
