package com.heroslender.herospawners.controllers;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.models.EntityProperties;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigurationController implements Controller {
    @Getter private int stackRadious;
    @Getter private int stackLimit;
    @Getter private List<String> hologramText;
    @Getter private int hologramViewDistance;
    private Map<EntityType, EntityProperties> entityProperties;

    @Getter private boolean dropXP;

    @Getter private ItemProperties itemProperties;


    @Getter private boolean requireSilktouch;
    @Getter private int silktouchLevel;
    @Getter private boolean destroySilktouch;

    @Override
    public void init() {
        loadDefaults();

        dropXP = getConfig().getBoolean("spawner.dropXP", false);

        itemProperties = new ItemProperties();
        itemProperties.name = parseColors(getConfig().getString("spawner.ItemStack.name", "&aSpawner de &7%tipo%"));
        itemProperties.lore = parseColors(getConfig().getStringList("spawner.ItemStack.lore"));
        itemProperties.loadProps();

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

                val entityProperty = new EntityProperties(name, head);
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
        setDefault("spawner.ItemStack.name", "&aSpawner de &7%tipo%");
        setDefault("spawner.ItemStack.lore", Collections.singletonList("&eQuantidade: &7x%quantidade%"));
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
        // Unused
    }

    public EntityProperties getProperties(final EntityType entityType) {
        return entityProperties.get(entityType);
    }

    @Data
    public class ItemProperties {
        private String name;
        private List<String> lore;

        private boolean amountInName;
        private int amountBeginIndex = -1;
        private int amountEndlength = -1;
        private int amountLine = -1;

        private void loadProps() {
            if (hasAmount(name)) {
                amountInName = true;
                loadIndexes(name);
            } else {
                amountInName = false;
                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (hasAmount(line)) {
                        amountLine = i;
                        loadIndexes(line);
                        return;
                    }
                }

                HeroSpawners.getInstance().getLogger().log(
                        Level.SEVERE,
                        "A variavel %quantidade% não se encontra presente no nome nem na lore do item."
                );
            }
        }

        private void loadIndexes(@NotNull final String text) {
            amountBeginIndex = text.indexOf("%quantidade%");
            amountEndlength = text.length() - (amountBeginIndex + "%quantidade%".length()) - 1;
        }

        private boolean hasAmount(@NotNull final String text) {
            return text.contains("%quantidade%");
        }
    }
}
