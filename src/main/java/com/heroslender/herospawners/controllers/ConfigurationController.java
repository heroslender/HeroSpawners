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

    @Getter private boolean dropXP;

    @Getter private boolean requireSilktouch;
    @Getter private int silktouchLevel;
    @Getter private boolean destroySilktouch;

    @Override
    public void init() {
        loadDefaults();

        dropXP = getConfig().getBoolean("spawner.dropXP", false);

        requireSilktouch = getConfig().getBoolean("spawner.SilkTouch.enable", false);
        silktouchLevel = getConfig().getInt("spawner.SilkTouch.minLevel", 1);
        destroySilktouch = getConfig().getBoolean("spawner.SilkTouch.detroySpawnerWithouSilktouch", true);

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
        String name, head;
        for (EntityType e : EntityType.values()) {
            if (e.getEntityClass() != null && e.getName() != null && LivingEntity.class.isAssignableFrom(e.getEntityClass())) {
                name = parseColors(getConfig().getString("mobs." + e.name() + ".name", e.getName()));
                head = getConfig().getString("mobs." + e.name() + ".head", "MHF_" + e.getName());

                val entityProperty = new EntityProperties(name,head);
                entityProperties.put(e, entityProperty);
            }
        }
    }

    private void loadDefaults() {
        HeroSpawners.getInstance().saveDefaultConfig();
        HeroSpawners.getInstance().reloadConfig();

        setDefault("juntar.maximo", 0);
        setDefault("juntar.raio", 5);

        setDefault("spawner.dropXP", false);
        setDefault("spawner.SilkTouch.enable", true);
        setDefault("spawner.SilkTouch.minLevel", 1);
        setDefault("spawner.SilkTouch.detroySpawnerWithouSilktouch", true);

        setDefault("holograma.distancia", 5);
        setDefault("holograma.texto", "&7%quantidade%x &e%tipo%");
        setDefault("holograma.mostrar-cabeca", true);

        for (EntityType e : EntityType.values()) {
            if (e.getEntityClass() != null && LivingEntity.class.isAssignableFrom(e.getEntityClass())) {
                setDefault("mobs." + e.name() + ".name", e.getName());
                setDefault("mobs." + e.name() + ".head", "MHF_" + e.getName());
            }
        }

        HeroSpawners.getInstance().saveConfig();
    }

    private void setDefault(String path, Object value) {
        if (!getConfig().isSet(path)) {
            getConfig().set(path, value);
        }
    }

    public boolean hasStackLimit() {
        return getStackLimit() <= 0;
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
