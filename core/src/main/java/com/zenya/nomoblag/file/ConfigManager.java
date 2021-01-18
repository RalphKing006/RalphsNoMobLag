package com.zenya.nomoblag.file;

import com.zenya.nomoblag.NoMobLag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ConfigManager {
    private int configVersion = 5; //Change this when updating config
    private boolean resetConfig = true; //Change this if config should reset when updating

    private static ConfigManager configManager;
    private Plugin plugin = NoMobLag.getInstance();
    private FileConfiguration origConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("config.yml")));
    private File configFile;
    private FileConfiguration config;

    public ConfigManager() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        if(!getConfigExists()) {
            plugin.saveDefaultConfig();
            return;
        }

        if(getConfigVersion() != configVersion) {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File oldConfigFile = new File(plugin.getDataFolder(), "config.yml.v" + String.valueOf(getConfigVersion()));
            FileUtil.copy(configFile, oldConfigFile);

            if(!resetConfig) {
                config.setDefaults(origConfig);
                config.options().copyDefaults(true);
                config.set("config-version", configVersion);
                plugin.saveConfig();
            } else {
                configFile.delete();
                plugin.saveDefaultConfig();
            }

        }

    }

    private boolean getConfigExists() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        return configFile.exists();
    }

    private int getConfigVersion() {
        return getInt("config-version");
    }

    public String getString(String node) {
        String val;
        try {
            val = config.getString(node);
        } catch(Exception e) {
            val = "";
        }
        return val;
    }

    public int getInt(String node) {
        int val;
        try {
            val = config.getInt(node);
        } catch(Exception e) {
            val = 0;
        }
        return val;
    }

    public double getDouble(String node) {
        double val;
        try {
            val = config.getDouble(node);
        } catch(Exception e) {
            val = 0d;
        }
        return val;
    }

    public boolean getBool(String node) {
        boolean val;
        try {
            val = config.getBoolean(node);
        } catch(Exception e) {
            val = false;
        }
        return val;
    }

    public ArrayList<String> getKeys(String node) {
        ArrayList<String> val = new ArrayList<String>();
        try {
            for(String key : config.getConfigurationSection(node).getKeys(false)) {
                val.add(key);
            }
        } catch(Exception e) {
            val = new ArrayList<String>();
            e.printStackTrace();
        }
        return val;
    }

    public ArrayList<String> getList(String node) {
        ArrayList<String> val = new ArrayList<String>();
        try {
            for(String s : config.getStringList(node)) {
                val.add(s);
            }
        } catch(Exception e) {
            val = new ArrayList<String>();
            e.printStackTrace();
        }
        return val;
    }

    public static void reloadConfig() {
        configManager = null;
        getInstance();
    }


    public static ConfigManager getInstance() {
        if(configManager == null) {
            configManager = new ConfigManager();
        }
        return configManager;
    }
}
