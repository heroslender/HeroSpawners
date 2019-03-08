package com.heroslender.herospawners.utils;

import com.heroslender.herospawners.HeroSpawners;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class Config {

    public static String TEXTO_HOLOGRAMA = "&7%quantidade%x &e%tipo%";
    public static int JUNTAR_RAIO = 5;
    public static int JUNTAR_MAX = 0;
    public static int SPAWNER_HOLOGRAM_VIEW_DISTANCE = 5;

    public static boolean showHeadHologram = true;
    private static Map<EntityType, String> entityHeads;
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
        if (!config.contains("holograma.mostrar_head"))
            config.set("juntar.mostrar_head", showHeadHologram);

        for (EntityType e : EntityType.values()) {
            if (e.getEntityClass() != null
                    && e.getEntityClass().isAssignableFrom(LivingEntity.class)) {
                if (!config.contains("mobs." + e.name() + ".name")) {
                    config.set("mobs." + e.name() + ".name", e.getName());
                }
                if (!config.contains("mobs." + e.name() + ".head")) {
                    config.set("mobs." + e.name() + ".head", "MHF_" + e.getName());
                }
            }
        }

        HeroSpawners.getInstance().saveConfig();
    }

    private static void loadConfig() {
        FileConfiguration config = HeroSpawners.getInstance().getConfig();
        TEXTO_HOLOGRAMA = ChatColor.translateAlternateColorCodes('&', config.getString("holograma.texto", TEXTO_HOLOGRAMA));
        JUNTAR_RAIO = config.getInt("juntar.raio", JUNTAR_RAIO);
        JUNTAR_MAX = config.getInt("juntar.maximo", JUNTAR_MAX);
        SPAWNER_HOLOGRAM_VIEW_DISTANCE = config.getInt("holograma.distancia", SPAWNER_HOLOGRAM_VIEW_DISTANCE);
        showHeadHologram = config.getBoolean("juntar.mostrar_head", showHeadHologram);

        if (entityNames == null)
            entityNames = new HashMap<>();
        else
            entityNames.clear();
        if (entityHeads == null)
            entityHeads = new HashMap<>();
        else
            entityHeads.clear();
        for (EntityType e : EntityType.values()) {
            entityNames.put(e, config.getString("mobs." + e.name() + ".name", e.getName()));
            entityHeads.put(e, config.getString("mobs." + e.name() + ".head", "MHF_" + e.getName()));
        }
    }

    public static String getNomeEntidade(EntityType entityType) {
        return entityNames.get(entityType);
    }

    public static String getHeadEntidade(EntityType entityType) {
        return entityHeads.get(entityType);
    }
}
