package com.Zenya.MobSpawnLimit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigManager {
    private static ConfigManager configManager;

    private static Plugin plugin;
    private static FileConfiguration config;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        if(!(this.getConfigExists())) {
            plugin.saveDefaultConfig();
        }
    }

    private boolean getConfigExists() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        return configFile.exists();
    }

    public static int getChanceAt(Integer tps) {
        int chance = config.getInt("spawn-chance." + tps.toString());
        return chance;
    }

    private String getConfigVersion() {
        String version = config.getString("config-version");
        return version;
    }

    public static ConfigManager getInstance() {
        if(configManager == null) {
            configManager = new ConfigManager(plugin);
        }
        return configManager;
    }
}
