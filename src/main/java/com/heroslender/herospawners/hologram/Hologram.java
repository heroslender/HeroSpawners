package com.heroslender.herospawners.hologram;

import org.bukkit.inventory.ItemStack;

public interface Hologram {
    void addLine(String line);

    void addLine(ItemStack item);

    void removeLine(int line);

    void setLine(int line, String text);

    int size();

    void remove();
}
