package com.heroslender.herospawners.models;

import com.google.common.collect.Lists;
import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.utils.Utilities;
import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.List;

import static com.heroslender.herospawners.controllers.ConfigurationController.AMOUNT_PLACEHOLDER;
import static com.heroslender.herospawners.controllers.ConfigurationController.TYPE_PLACEHOLDER;

@AllArgsConstructor
@RequiredArgsConstructor
public class Spawner implements ISpawner {
    private static final String[] placeholders = new String[]{"%dono%", AMOUNT_PLACEHOLDER, TYPE_PLACEHOLDER};
    @Getter private final String owner;
    private final Location location;
    @Getter @Setter private boolean updateRequired = false;
    @Getter private int amount;

    // Lazy loaded field
    private EntityProperties entityProperties = null;

    public Spawner(String owner, Location location, int amount) {
        this.owner = owner;
        this.location = location;
        this.amount = amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.updateRequired = true;
    }

    public Location getLocation() {
        return location.clone();
    }

    @Override
    public CreatureSpawner getState() {
        return (CreatureSpawner) getLocation().getBlock().getState();
    }

    @Override
    public EntityType getType() {
        return  getState().getSpawnedType();
    }

    @Override
    public synchronized EntityProperties getEntityProperties() {
        if (this.entityProperties == null) {
            this.entityProperties = HeroSpawners.getInstance().getConfigurationController().getProperties(getType());
        }

        return this.entityProperties;
    }

    @Override
    public List<String> getHologramText() {
        List<String> hologramLines = Lists.newArrayList();
        val values = new String[]{getOwner(), Integer.toString(getAmount()), getEntityProperties().getDisplayName()};

        for (String line : HeroSpawners.getInstance().getConfigurationController().getHologramText()) {
            hologramLines.add(StringUtils.replaceEach(line, placeholders, values));
        }

        return hologramLines;
    }

    @Override
    public String[] getInformationText() {
        final List<String> informationText = HeroSpawners.getInstance().getConfigurationController().getInformationText();
        final String[] values = new String[]{getOwner(), Integer.toString(getAmount()), getEntityProperties().getDisplayName()};

        String[] info = new String[informationText.size()];
        for (int i = 0; i < informationText.size(); i++) {
            info[i] = StringUtils.replaceEach(informationText.get(i), placeholders, values);
        }

        return info;
    }

    @Override
    public String toString() {
        return "Spawner(location=\"" + Utilities.loc2str(getLocation()) + "\", quantidade=\"" + getAmount() + "\")";
    }
}
