package com.heroslender.herospawners.utils;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class Utilities {
    @Getter private static final SplittableRandom random = new SplittableRandom();

    public static List<Block> getBlocks(Block start, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(start.getRelative(x, y, z));
                }
            }
        }
        blocks.remove(start);
        return blocks;
    }

    public static Location str2loc(String str) {
        String[] str2loc = str.split("\\|");
        return str2loc(str2loc);
    }

    public static Location str2loc(String[] str) {
        return new Location(
                Bukkit.getServer().getWorld(str[0]),
                Double.parseDouble(str[1]),
                Double.parseDouble(str[2]),
                Double.parseDouble(str[3])
        );
    }

    public static String loc2str(Location loc) {
        return loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
    }
}


