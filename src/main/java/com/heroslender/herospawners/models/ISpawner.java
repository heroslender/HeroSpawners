package com.heroslender.herospawners.models;

import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.List;

public interface ISpawner {

    /**
     * The amount of stacked spawners
     *
     * @return Stack amount
     */
    int getAmount();

    /**
     * Update the spawner stack amount
     *
     * @param amount new amount to set
     */
    void setAmount(int amount);

    /**
     * The location of the spawner
     *
     * @return A clone of the spawners location
     */
    Location getLocation();

    /**
     * The owner of the spawner
     *
     * @return The spawners owner
     */
    String getOwner();

    /**
     * Get the state of the spawner
     *
     * @return The spawner state
     */
    CreatureSpawner getState();

    /**
     * Get the type of entity the spawner is spawning
     *
     * @return The entity type
     */
    EntityType getType();

    /**
     * Get the entity properties for the spawner's
     * spawning entity
     *
     * @return The entity properties
     */
    EntityProperties getEntityProperties();

    /**
     * Get line values for the hologram.
     * @return The line values
     */
    List<String> getHologramText();
}
