package com.heroslender.herospawners.models;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.utils.Utilities;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequiredArgsConstructor
public class Spawner implements ISpawner {
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
        return HeroSpawners.getInstance().getConfigurationController().getHologramText()
                .stream()
                .map(h -> h
                        .replace("%dono%", getOwner())
                        .replace("%quantidade%", Integer.toString(getAmount()))
                        .replace("%tipo%", getEntityProperties().getDisplayName()))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Spawner(location=\"" + Utilities.loc2str(getLocation()) + "\", quantidade=\"" + getAmount() + "\")";
    }
}
