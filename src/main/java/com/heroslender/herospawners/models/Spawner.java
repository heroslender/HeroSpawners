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

@AllArgsConstructor
@RequiredArgsConstructor
public class Spawner implements ISpawner {
    private static final String[] placeholders = new String[]{"%dono%", "%quantidade%", "%tipo%"};
    @Getter private final String owner;
    private final Location location;
    @Getter @Setter private boolean updateRequired = false;
    @Getter private int amount;

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
        return getState().getSpawnedType();
    }

    @Override
    public EntityProperties getEntityProperties() {
        return HeroSpawners.getInstance().getConfigurationController().getProperties(getType());
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
    public String toString() {
        return "Spawner(location=\"" + Utilities.loc2str(getLocation()) + "\", quantidade=\"" + getAmount() + "\")";
    }
}
