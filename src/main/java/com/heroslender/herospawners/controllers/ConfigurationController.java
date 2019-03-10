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
import java.util.Map;

@RequiredArgsConstructor
public class ConfigurationController implements Controller {
    private final ConfigurationService configurationService;

    @Getter private int stackRadious;
    @Getter private int stackLimit;
    @Getter private String hologramText;
    @Getter private int hologramViewDistance;
    @Getter private boolean hologramShowHead;
    private Map<EntityType, EntityProperties> entityProperties;

    @Override
    public void init() {
        configurationService.init();

        stackRadious = configurationService.getConfig().getInt("juntar.raio", 5);
        stackLimit = configurationService.getConfig().getInt("juntar.maximo", 0);

        hologramText = parseColors(configurationService.getConfig().getString("holograma.texto", "&7%quantidade%x &e%tipo%"));
        hologramViewDistance = configurationService.getConfig().getInt("holograma.distancia", 0);
        hologramShowHead = configurationService.getConfig().getBoolean("holograma.mostrar-cabeca", true);

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

    @Override
    public void stop() {
        configurationService.stop();
    }

    public EntityProperties getProperties(final EntityType entityType) {
        return entityProperties.get(entityType);
    }
}
