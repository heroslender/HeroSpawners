package com.heroslender.herospawners.models;

import org.bukkit.Location;

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
}
