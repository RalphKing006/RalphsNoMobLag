package com.zenya.nomoblag.file;

import com.zenya.nomoblag.NoMobLag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MessagesManager {
    private int messagesVersion = 1; //Change this when updating messages
    private boolean resetMessages = false; //Change this if messages should reset when updating

    private static MessagesManager messagesManager;
    private Plugin plugin = NoMobLag.getInstance();
    private FileConfiguration origMessages = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml")));
    private File messagesFile;
    private FileConfiguration messages;

    public MessagesManager() throws IOException {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        if(!getMessagesExists()) {
            origMessages.save(messagesFile);
            return;
        }

        if(getMessagesVersion() != messagesVersion) {
            File oldMessagesFile = new File(plugin.getDataFolder(), "messages.yml.v" + String.valueOf(getMessagesVersion()));
            FileUtil.copy(messagesFile, oldMessagesFile);

            if(!resetMessages) {
                messages.setDefaults(origMessages);
                messages.options().copyDefaults(true);
                messages.set("messages-version", messagesVersion);
                messages.save(messagesFile);
            } else {
                messagesFile.delete();
                origMessages.save(messagesFile);
            }

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
        } catch(Exception e) {
            val = "";
        }
        return val;
    }

    private int getInt(String node) {
        int val;
        try {
            val = messages.getInt(node);
        } catch(Exception e) {
            val = 0;
        }
        return val;
    }

    public static void reloadMessages() {
        messagesManager = null;
        getInstance();
    }


    public static MessagesManager getInstance() {
        if(messagesManager == null) {
            try {
                messagesManager = new MessagesManager();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messagesManager;
    }
}
