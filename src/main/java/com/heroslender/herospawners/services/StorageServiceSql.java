package com.heroslender.herospawners.services;

public interface StorageServiceSql extends StorageService {
    String SPAWNERS = "spawners";
    String SPAWNERS_OWNER = "owner";
    String SPAWNERS_LOC = "location";
    String SPAWNERS_QUANT = "quantidade";
    String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + SPAWNERS + " (" +
            "`" + SPAWNERS_OWNER + "` varchar(64) NOT NULL," +
            "`" + SPAWNERS_LOC + "` varchar(128) NOT NULL," +
            "`" + SPAWNERS_QUANT + "` varchar(32) NOT NULL," +
            "PRIMARY KEY (`" + SPAWNERS_LOC + "`)" +
            ");";
    String TABLE_ADD_OWNER_COLUMN = "ALTER TABLE `" + SPAWNERS + "` ADD `" + SPAWNERS_OWNER + "` varchar(64) NOT NULL default 'Ninguem';";
}
