package com.heroslender.herospawners.controllers;

import com.heroslender.herospawners.models.EntityProperties;
import com.heroslender.herospawners.services.ConfigurationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigurationController implements Controller {
    private final ConfigurationService configurationService;

    @Getter private String messagePlace;
    @Getter private String messageBreak;

    @Getter private int stackRadious;
    @Getter private int stackLimit;
    private Map<EntityType, EntityProperties> entityProperties;

    @Override
    public void init() {
        configurationService.init();

        messagePlace = configurationService.getConfig().getString("msg.colocar").replace('&', '§');
        messageBreak = configurationService.getConfig().getString("msg.quebrar").replace('&', '§');

        stackRadious = configurationService.getConfig().getInt("juntar.raio", 5);
        stackLimit = configurationService.getConfig().getInt("juntar.maximo", 0);

        entityProperties = new EnumMap<>(EntityType.class);
        for (EntityType e : EntityType.values()) {
            if (e.getEntityClass() != null
                    && e.getName() != null
                    && LivingEntity.class.isAssignableFrom(e.getEntityClass())) {
                val entityProperty = new EntityProperties(
                        parseColors(configurationService.getConfig().getString("mobs." + e.name() + ".name", e.getName())),
                        configurationService.getConfig().getString("mobs." + e.name() + ".head", "MHF_" + e.getName())
                );
                entityProperties.put(e, entityProperty);
            }
        }
    }

    private String parseColors(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private List<String> parseColors(final List<String> strings) {
        return strings.stream().map(this::parseColors).collect(Collectors.toList());
    }

    @Override
    public void stop() {
        configurationService.stop();
    }

    public EntityProperties getProperties(final EntityType entityType) {
        return entityProperties.get(entityType);
    }
}
