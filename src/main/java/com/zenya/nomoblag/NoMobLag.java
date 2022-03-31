package com.zenya.nomoblag;

import com.zenya.nomoblag.command.NoMobLagCommand;
import com.zenya.nomoblag.command.NoMobLagTab;
import com.zenya.nomoblag.event.Listeners;
import com.zenya.nomoblag.file.ConfigManager;
import com.zenya.nomoblag.file.MessagesManager;
import com.zenya.nomoblag.scheduler.TaskManager;
import com.zenya.nomoblag.util.MetricsLite;
import com.zenya.nomoblag.util.Updater;
import com.zenya.nomoblag.util.Updater.UpdateResult;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class NoMobLag extends JavaPlugin {

    private static NoMobLag instance;

    @Override
    public void onEnable() {
        instance = this;

        new MetricsLite(this, 13821);
        checkForUpdate();

        //Register all runnables
        TaskManager.getInstance();

        //Init config and messages
        ConfigManager.getInstance();
        MessagesManager.getInstance();

        //Register events
        this.getServer().getPluginManager().registerEvents(new Listeners(), this);

        //Register commands
        this.getCommand("nomoblag").setExecutor(new NoMobLagCommand());
        this.getCommand("nomoblag").setTabCompleter(new NoMobLagTab());
    }

    public static NoMobLag instance() {
        return instance;
    }

    private void checkForUpdate() {
        Logger logger = getLogger();
        FileConfiguration pluginConfig = getConfig();
        Updater updater = new Updater(this, 98912, false);
        Updater.UpdateResult result = updater.getResult();
        if (result != UpdateResult.UPDATE_AVAILABLE) {
            return;
        }
        if (!pluginConfig.getBoolean("download-update")) {
            logger.info("===== UPDATE AVAILABLE ====");
            logger.info("https://www.spigotmc.org/resources/98912");
            logger.log(Level.INFO, "Installed Version: {0} New Version:{1}", new Object[]{updater.getOldVersion(), updater.getVersion()});
            logger.info("===== UPDATE AVAILABLE ====");
            return;
        }
        logger.info("==== UPDATE AVAILABLE ====");
        logger.info("====    DOWNLOADING   ====");
        updater.downloadUpdate();
    }

}
