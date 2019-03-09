package com.heroslender.herospawners.services;

import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigurationService extends Service {

    FileConfiguration getConfig();
}
