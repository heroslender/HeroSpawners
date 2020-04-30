package com.heroslender.herospawners.controllers;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.EntityProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigurationController implements Controller {
    @Getter private int stackRadious;
    @Getter private int stackLimit;
    @Getter private List<String> hologramText;
    @Getter private int hologramViewDistance;
    private Map<EntityType, EntityProperties> entityProperties;

    @Override
    public void init() {
        loadDefaults();

        stackRadious = getConfig().getInt("juntar.raio", 5);
        stackLimit = getConfig().getInt("juntar.maximo", 0);

        if (getConfig().isList("holograma.texto")) {
            hologramText = parseColors(getConfig().getStringList("holograma.texto"));
        } else {
            hologramText = new ArrayList<>();
            hologramText.add(parseColors(getConfig().getString("holograma.texto", "&7%quantidade%x &e%tipo%")));
        }

        hologramViewDistance = getConfig().getInt("holograma.distancia", 0);

        entityProperties = new EnumMap<>(EntityType.class);
        for (EntityType e : EntityType.values()) {
            if (e.getEntityClass() != null
                    && e.getName() != null
                    && LivingEntity.class.isAssignableFrom(e.getEntityClass())) {
                val entityProperty = new EntityProperties(
                        parseColors(getConfig().getString("mobs." + e.name() + ".name", e.getName())),
                        getConfig().getString("mobs." + e.name() + ".head", "MHF_" + e.getName())
                );
                entityProperties.put(e, entityProperty);
            }
        }
    }

    private void loadDefaults() {
        HeroSpawners.getInstance().saveDefaultConfig();
        HeroSpawners.getInstance().reloadConfig();
        final FileConfiguration configuration = HeroSpawners.getInstance().getConfig();

        if (!configuration.contains("juntar.maximo"))
            configuration.set("juntar.maximo", 0);
        if (!configuration.contains("juntar.raio"))
            configuration.set("juntar.raio", 5);

        if (!configuration.contains("holograma.distancia"))
            configuration.set("holograma.distancia", 5);
        if (!configuration.contains("holograma.texto"))
            configuration.set("holograma.texto", "&7%quantidade%x &e%tipo%");
        if (!configuration.contains("holograma.mostrar-cabeca"))
            configuration.set("holograma.mostrar-cabeca", true);

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

    private FileConfiguration getConfig() {
        return HeroSpawners.getInstance().getConfig();
    }

    private String parseColors(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private List<String> parseColors(final List<String> strings) {
        return strings.stream().map(this::parseColors).collect(Collectors.toList());
    }

    @Override
    public void stop() {
    }

    public EntityProperties getProperties(final EntityType entityType) {
        return entityProperties.get(entityType);
    }
}
