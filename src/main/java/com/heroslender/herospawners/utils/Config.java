package com.heroslender.herospawners.utils;

import com.heroslender.herospawners.HeroSpawners;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public static String TEXTO_HOLOGRAMA = "&7%quantidade%x &e%tipo%";
    public static int JUNTAR_RAIO = 5;
    public static int JUNTAR_MAX = 0;
    public static int SPAWNER_HOLOGRAM_VIEW_DISTANCE = 5;

    private static Map<EntityType, String> entityNames;

    public static void init() {
        verificarConfig();
        loadConfig();
    }

    private static void verificarConfig() {
        FileConfiguration config = HeroSpawners.getInstance().getConfig();
        if (!config.contains("holograma.distancia"))
            config.set("holograma.distancia", SPAWNER_HOLOGRAM_VIEW_DISTANCE);
        if (!config.contains("holograma.texto"))
            config.set("holograma.texto", TEXTO_HOLOGRAMA);
        if (!config.contains("juntar.maximo"))
            config.set("juntar.maximo", JUNTAR_MAX);
        if (!config.contains("juntar.raio"))
            config.set("juntar.raio", JUNTAR_RAIO);

        for (EntityType e : EntityType.values()) {
            if (!config.contains("mobs." + e.name()))
                config.set("mobs." + e.name(), e.getName());
        }

        HeroSpawners.getInstance().saveConfig();
    }

    private static void loadConfig() {
        FileConfiguration config = HeroSpawners.getInstance().getConfig();
        TEXTO_HOLOGRAMA = ChatColor.translateAlternateColorCodes('&', config.getString("holograma.texto", TEXTO_HOLOGRAMA));
        JUNTAR_RAIO = config.getInt("juntar.raio", JUNTAR_RAIO);
        JUNTAR_MAX = config.getInt("juntar.maximo", JUNTAR_MAX);
        SPAWNER_HOLOGRAM_VIEW_DISTANCE = config.getInt("holograma.distancia", SPAWNER_HOLOGRAM_VIEW_DISTANCE);

        if (entityNames == null)
            entityNames = new HashMap<>();
        else
            entityNames.clear();
        for (EntityType e : EntityType.values()) {
            entityNames.put(e, config.getString("mobs." + e.name(), e.getName()));
        }
    }

    public static String getNomeEntidade(EntityType entityType) {
        return entityNames.get(entityType);
    }
}
