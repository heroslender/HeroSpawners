package com.heroslender.HeroSpawners.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class Utilities {

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
        String str2loc[] = str.split("\\|");
        return new Location(Bukkit.getServer().getWorld(str2loc[0]), Double.parseDouble(str2loc[1]), Double.parseDouble(str2loc[2]), Double.parseDouble(str2loc[3]));
    }

    public static String loc2str(Location loc) {
        return loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
    }
//    private HeroSpawners ss;
//    private Random rand = new Random();

//    Utilities(HeroSpawners ss) {
//        this.ss = ss;
//    }

//    private static Location getHologramLoc(Location spawnerLoc) {
//        return spawnerLoc.clone().add(0.5, 1.4, 0.5);
//    }

//    public Random random() {
//        return this.rand;
//    }

//    private String getStringHolograma(Location loc){
//        return getStringHolograma(loc, ss.spawnerAmount.get(loc));
//    }

//    private String getStringHolograma(Location loc, int quatidade){
//        return ChatColor.translateAlternateColorCodes('&', ss.getConfig().getString("spawner.holograma"))
//                .replace("%quantidade%", quatidade+ "")
//                .replace("%tipo%", Config.getNomeEntidade(((CreatureSpawner) loc.getBlock().getState()).getSpawnedType()));
//    }

//    public void atualizaHolograma(Location spawnerLoc) {
//        try {
//            String a = getStringHolograma(spawnerLoc);
//            for (Hologram hologram : HologramsAPI.getHolograms(ss)) {
//                if (hologram.getLocation().equals(getHologramLoc(spawnerLoc))) {
//                    hologram.appendTextLine(a);
//                    hologram.removeLine(0);
//                    return;
//                }
//            }
//            Hologram h = HologramsAPI.createHologram(ss, Utilities.getHologramLoc(spawnerLoc));
//            try {
//                h.appendTextLine(a);
//            } catch (Exception e) {
//                h.delete();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public void atualizaHolograma(Location spawnerLoc, int quantidade) {
//        try {
//            if (Bukkit.isPrimaryThread()) {
//                String a = getStringHolograma(spawnerLoc, quantidade);
//                for (Hologram hologram : HologramsAPI.getHolograms(ss)) {
//                    if (hologram.getLocation().equals(getHologramLoc(spawnerLoc))) {
//                        hologram.appendTextLine(a);
//                        hologram.removeLine(0);
//                        return;
//                    }
//                }
//                Hologram h = HologramsAPI.createHologram(ss, Utilities.getHologramLoc(spawnerLoc));
//                try {
//                    h.appendTextLine(a);
//                } catch (Exception e) {
//                    h.delete();
//                }
//            } else {
//                Bukkit.getScheduler().scheduleSyncDelayedTask(ss, () -> {
//                    String a = getStringHolograma(spawnerLoc, quantidade);
//                    for (Hologram hologram : HologramsAPI.getHolograms(ss)) {
//                        if (hologram.getLocation().equals(getHologramLoc(spawnerLoc))) {
//                            hologram.appendTextLine(a);
//                            hologram.removeLine(0);
//                            return;
//                        }
//                    }
//                    Hologram h = HologramsAPI.createHologram(ss, Utilities.getHologramLoc(spawnerLoc));
//                    try {
//                        h.appendTextLine(a);
//                    } catch (Exception e) {
//                        h.delete();
//                    }
//                });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public void apagaHolograma(Location location){
//        for (Hologram hologram : HologramsAPI.getHolograms(ss))
//            if (hologram.getLocation().equals(getHologramLoc(location)))
//                hologram.delete();
//    }

//    LivingEntity getArmorStand(Block block) {
//        for (Entity e : block.getWorld().getNearbyEntities(block.getLocation().add(0.5D, 0.0D, 0.5D), 0.2D, 0.1D, 0.2D)) {
//            if (e.getType() == EntityType.ARMOR_STAND) {
//                return (LivingEntity) e;
//            }
//        }
//        return null;
//    }

//    public void createNewArmorstand(Block e) {
//        ArmorStand am = (ArmorStand) e.getWorld().spawnEntity(e.getLocation().add(0.5D, 0.0D, 0.5D), EntityType.ARMOR_STAND);
//        am.setSmall(true);
//        am.setVisible(false);
//        am.setGravity(false);
////        if (!Bukkit.getVersion().contains("1.8")) {
////            am.setInvulnerable(true);
////        }
//        am.setCustomNameVisible(this.ss.getConfig().getBoolean("spawner.nametag.always-visible"));
//        this.ss.spawnerAmount.put(e.getLocation(), Integer.valueOf(1));
//        if (this.ss.getConfig().getBoolean("spawner.nametag.display")) {
//            this.ss.util.updateTag(am);
//        }
//    }

//    public void updateTag(ArmorStand as) {
//        CreatureSpawner cs = (CreatureSpawner) as.getLocation().getBlock().getState();
//        String a = ChatColor.translateAlternateColorCodes('&', this.ss.getConfig().getString("spawner.nametag.format"));
//        String b = a.replace("%amount%", ((Integer) this.ss.spawnerAmount.get(cs.getLocation())).toString()).replace("%type%", StringUtils.capitalize(cs.getCreatureTypeName().replace("_", " ")));
//        as.setCustomName(b);
//    }
}


