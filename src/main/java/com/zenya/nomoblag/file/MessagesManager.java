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

public class MessagesManager {
  //Change this when updating messages

  private int messagesVersion = 1;
  //Change this if messages should reset when updating
  private boolean resetMessages = false;
  //These nodes will use the latest resource config's values
  private List<String> ignoredNodes = new ArrayList<String>() {
    {
      add("messages-version");
    }
  };
  //These nodes will be emptied and replaced with old values instead of being appended
  //Applicable to nested keys
  private List<String> replaceNodes = new ArrayList<String>() {
    {
    }
  };

  private static MessagesManager messagesManager;
  private Plugin plugin = NoMobLag.getInstance();
  private FileConfiguration origMessages = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml")));
  private File messagesFile;
  private FileConfiguration messages;

  public MessagesManager() throws IOException {
    messagesFile = new File(plugin.getDataFolder(), "messages.yml");
    if (!getMessagesExists()) {
      origMessages.save(messagesFile);
    }
    messages = YamlConfiguration.loadConfiguration(messagesFile);

    //Reset messages for backward-compatibility
    if (getMessagesVersion() > messagesVersion) {
      resetMessages = true;
    }

    if (getMessagesVersion() != messagesVersion) {
      File oldMessagesFile = new File(plugin.getDataFolder(), "messages.yml.v" + String.valueOf(getMessagesVersion()));
      FileUtil.copy(messagesFile, oldMessagesFile);
      FileConfiguration oldMessages = YamlConfiguration.loadConfiguration(oldMessagesFile);

      //Refresh file
      messagesFile.delete();
      origMessages.save(messagesFile);
      messagesFile = new File(plugin.getDataFolder(), "messages.yml");
      messages = YamlConfiguration.loadConfiguration(messagesFile);

      //Add old values
      if (!resetMessages) {
        for (String node : oldMessages.getKeys(true)) {
          if (ignoredNodes.contains(node)) {
            continue;
          }
          if (oldMessages.getKeys(true).contains(node + ".")) {
            continue;
          }
          if (replaceNodes.contains(node)) {
            messages.set(node, null);
            messages.createSection(node);
          }
          messages.set(node, oldMessages.get(node));
        }
      }

      //Save regardless
      messages.save(messagesFile);
    }
  }

  private boolean getMessagesExists() {
    return messagesFile.exists();
  }

  private int getMessagesVersion() {
    return getInt("messages-version");
  }

  public String getString(String node) {
    String val;
    try {
      val = messages.getString(node);
    } catch (Exception e) {
      val = "";
    }
    return val;
  }

  private int getInt(String node) {
    int val;
    try {
      val = messages.getInt(node);
    } catch (Exception e) {
      val = 0;
    }
    return val;
  }

  public static void reloadMessages() {
    messagesManager = null;
    getInstance();
  }

  public static MessagesManager getInstance() {
    if (messagesManager == null) {
      try {
        messagesManager = new MessagesManager();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return messagesManager;
  }
}
