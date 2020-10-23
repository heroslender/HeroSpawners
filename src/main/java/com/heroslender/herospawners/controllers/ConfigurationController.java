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
    public static final String SKULL_PLACEHOLDER = "%skull%";
    public static final String TYPE_PLACEHOLDER = "%tipo%";
    public static final String AMOUNT_PLACEHOLDER = "%quantidade%";

    @Getter private int stackRadious;
    @Getter private int stackLimit;

    @Getter private List<String> informationText;
    @Getter private long informationDelay;

    @Getter private List<String> hologramText;
    @Getter private int hologramViewDistance;
    private boolean trimHologram = false;

    private Map<EntityType, EntityProperties> entityProperties;


    @Getter private boolean spawnersEnabled;
    @Getter private boolean spawnersDropXP;

    @Getter private ItemProperties itemProperties;

    @Getter private boolean requireSilktouch;
    @Getter private int silktouchLevel;
    @Getter private boolean destroySilktouch;

    @Getter private boolean vanillaEnabled;

    @Override
    public void init() {
        loadDefaults();

        vanillaEnabled = getConfig().getBoolean("spawner.allow-break-vanilla-spawners", false);

        spawnersEnabled = getConfig().getBoolean("spawner.enable", false);
        spawnersDropXP = getConfig().getBoolean("spawner.dropXP", false);

        itemProperties = new ItemProperties();
        itemProperties.name = parseColors(getConfig().getString("spawner.ItemStack.name", "&aSpawner de &7" + TYPE_PLACEHOLDER));
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
            hologramText.add(parseColors(getConfig().getString("holograma.texto", "&7%quantidade%x &e" + TYPE_PLACEHOLDER)));
        }

        if (getConfig().isList("interact.texto")) {
            informationText = parseColors(getConfig().getStringList("interact.texto"));
        } else {
            informationText = new ArrayList<>();
            informationText.add(parseColors(getConfig().getString("interact.texto", "&7%quantidade%x &e" + TYPE_PLACEHOLDER)));
        }
        informationDelay = getConfig().getLong("interact.delay", 5000);

        hologramViewDistance = getConfig().getInt("holograma.distancia", 0);
        trimHologram = hologramText.get(hologramText.size() - 1).equalsIgnoreCase(SKULL_PLACEHOLDER);

        entityProperties = new EnumMap<>(EntityType.class);
        String name;
        String head;
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

        setDefault("spawner.enable", true);
        setDefault("spawner.dropXP", false);
        setDefault("spawner.allow-break-vanilla-spawners", false);
        setDefault("spawner.ItemStack.name", "&aSpawner de &7" + TYPE_PLACEHOLDER);
        setDefault("spawner.ItemStack.lore", Collections.singletonList("&eQuantidade: &7x" + AMOUNT_PLACEHOLDER));
        setDefault("spawner.SilkTouch.enable", true);
        setDefault("spawner.SilkTouch.minLevel", 1);
        setDefault("spawner.SilkTouch.detroySpawnerWithouSilktouch", true);

        setDefault("holograma.ativar", true);
        setDefault("holograma.distancia", 5);
        setDefault("holograma.texto", "&7" + AMOUNT_PLACEHOLDER + "x &e" + TYPE_PLACEHOLDER);
        setDefault("holograma.mostrar-cabeca", true);

        setDefault("interact.ativar", true);
        setDefault("interact.delay", 5000);
        setDefault("interact.texto", "&7" + AMOUNT_PLACEHOLDER + "x &e" + TYPE_PLACEHOLDER);

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

    public boolean trimHologram() {
        return trimHologram;
    }

    public boolean hasStackLimit() {
        return getStackLimit() > 0;
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
        private boolean deductEntityName;
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
                        "A variavel {0} não se encontra presente no nome nem na lore do item.",
                        AMOUNT_PLACEHOLDER
                );
            }
        }

        private void loadIndexes(@NotNull final String text) {
            deductEntityName = text.contains(TYPE_PLACEHOLDER);
            amountBeginIndex = text.indexOf(AMOUNT_PLACEHOLDER);
            amountEndlength = text.length() - (amountBeginIndex + AMOUNT_PLACEHOLDER.length());
        }

        private boolean hasAmount(@NotNull final String text) {
            return text.contains(AMOUNT_PLACEHOLDER);
        }
    }
}
