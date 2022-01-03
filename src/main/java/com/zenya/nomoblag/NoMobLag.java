package com.zenya.nomoblag;

import com.zenya.nomoblag.command.NoMobLagCommand;
import com.zenya.nomoblag.command.NoMobLagTab;
import com.zenya.nomoblag.event.Listeners;
import com.zenya.nomoblag.file.ConfigManager;
import com.zenya.nomoblag.file.MessagesManager;
import com.zenya.nomoblag.scheduler.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NoMobLag extends JavaPlugin {
    private static NoMobLag instance;

    public void onEnable() {
        instance = this;

        //Register all runnables
        TaskManager.getInstance();

        //Init config and messages
        ConfigManager.getInstance();
        MessagesManager.getInstance();

        //Register events
        this.getServer().getPluginManager().registerEvents(new Listeners(), this);

        //Register commands
        this.getCommand("nomoblag").setExecutor(new NoMobLagCommand());
        try {
            this.getCommand("nomoblag").setTabCompleter(new NoMobLagTab());
        } catch(Exception exc) {
            //Do nothing, version doesn't support tabcomplete
        }
    }

    public void onDisable() {

    }

    public static NoMobLag getInstance() {
        return instance;
    }
}
