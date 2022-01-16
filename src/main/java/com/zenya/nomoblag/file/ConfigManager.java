package com.zenya.nomoblag.file;

import com.zenya.nomoblag.NoMobLag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
  
  //Change this when updating config
  private int configVersion = 7;
  //Change this if config should reset when updating
  private boolean resetConfig = false;
  //These nodes will use the latest resource config's values
  private List<String> ignoredNodes = new ArrayList<String>() {
    {
      add("config-version");
    }
  };
  //These nodes will be emptied and replaced with old values instead of being appended
  //Applicable to keys and lists
  private List<String> replaceNodes = new ArrayList<String>() {
    {
      add("mob-spawning.spawn-chance-at-playercount");
      add("mob-spawning.spawnreason-tps-block");
    }
  };

  private static ConfigManager configManager;
  private Plugin plugin = NoMobLag.getInstance();
  private FileConfiguration origConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("config.yml")));
  private File configFile;
  private FileConfiguration config;

  public ConfigManager() throws IOException {
    configFile = new File(plugin.getDataFolder(), "config.yml");
    if (!getConfigExists()) {
      plugin.saveDefaultConfig();
    }
    config = YamlConfiguration.loadConfiguration(configFile);

    //Reset config for backward-compatibility
    if (getConfigVersion() > configVersion) {
      resetConfig = true;
    }

    if (getConfigVersion() != configVersion) {
      File oldConfigFile = new File(plugin.getDataFolder(), "config.yml.v" + String.valueOf(getConfigVersion()));
      FileUtil.copy(configFile, oldConfigFile);
      FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);

      //Refresh file
      configFile.delete();
      plugin.saveDefaultConfig();
      configFile = new File(plugin.getDataFolder(), "config.yml");
      config = YamlConfiguration.loadConfiguration(configFile);

      //Add old values
      if (!resetConfig) {
        for (String node : oldConfig.getKeys(true)) {
          if (ignoredNodes.contains(node)) {
            continue;
          }
          if (replaceNodes.contains(node)) {
            config.set(node, null);
          }
          if (oldConfig.getConfigurationSection(node) != null && !oldConfig.getConfigurationSection(node).getKeys(false).isEmpty()) {
            continue;
          }
          config.set(node, oldConfig.get(node));
        }
      }

      //Save regardless
      config.save(configFile);
    }
  }

  private boolean getConfigExists() {
    return new File(plugin.getDataFolder(), "config.yml").exists();
  }

  private int getConfigVersion() {
    return getInt("config-version");
  }

  public String getString(String node) {
    String val;
    try {
      val = config.getString(node);
    } catch (Exception e) {
      val = "";
    }
    return val;
  }

  public int getInt(String node) {
    int val;
    try {
      val = config.getInt(node);
    } catch (Exception e) {
      val = 0;
    }
    return val;
  }

  public double getDouble(String node) {
    double val;
    try {
      val = config.getDouble(node);
    } catch (Exception e) {
      val = 0d;
    }
    return val;
  }

  public boolean getBool(String node) {
    boolean val;
    try {
      val = config.getBoolean(node);
    } catch (Exception e) {
      val = false;
    }
    return val;
  }

  public ArrayList<String> getKeys(String node) {
    ArrayList<String> val = new ArrayList<>();
    try {
      for (String key : config.getConfigurationSection(node).getKeys(false)) {
        val.add(key);
      }
    } catch (Exception e) {
      val = new ArrayList<>();
      e.printStackTrace();
    }
    return val;
  }

  public ArrayList<String> getList(String node) {
    ArrayList<String> val = new ArrayList<>();
    try {
      for (String s : config.getStringList(node)) {
        val.add(s);
      }
    } catch (Exception e) {
      val = new ArrayList<>();
      e.printStackTrace();
    }
    return val;
  }

  public boolean listContains(String node, String item) {
    List<String> list = getList(node);
    if (list != null && !list.isEmpty() && list.contains(item)) {
      return true;
    }
    return false;
  }

  public static void reloadConfig() {
    configManager = null;
    getInstance();
  }

  public static ConfigManager getInstance() {
    if (configManager == null) {
      try {
        configManager = new ConfigManager();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return configManager;
  }
}
